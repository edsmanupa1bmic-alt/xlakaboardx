package org.fossify.keyboard.helpers

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import org.fossify.keyboard.extensions.safeStorageContext
import org.fossify.keyboard.models.ParsedFlorisTheme
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FlorisThemeParser {

    private const val TAG = "FlorisThemeParser"

    /**
     * Parses a standalone FlorisBoard JSON stylesheet/theme or imports a zipped .flex/.zip package.
     */
    fun importThemeFromUri(context: Context, uri: Uri): Result<ParsedFlorisTheme> {
        return try {
            var fileName = "Imported_Theme"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }

            val isZipPackage = fileName.endsWith(".zip", ignoreCase = true) ||
                               fileName.endsWith(".flex", ignoreCase = true)

            val theme = if (isZipPackage) {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Failed to open theme stream")
                extractAndParseZipTheme(context, inputStream, fileName.substringBeforeLast("."))
            } else {
                val jsonString = context.contentResolver.openInputStream(uri)?.use {
                    InputStreamReader(it).readText()
                } ?: throw IllegalArgumentException("Failed to read JSON theme")
                parseJsonTheme(jsonString, fileName.substringBeforeLast("."), null)
            }

            ThemeEngine.saveCustomTheme(context, theme)
            Result.success(theme)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import Floris theme: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Extracts a .flex/.zip archive to the internal themes directory and parses its stylesheet.
     */
    fun extractAndParseZipTheme(context: Context, inputStream: InputStream, defaultName: String): ParsedFlorisTheme {
        val themeId = "floris_pkg_" + UUID.randomUUID().toString().take(8)
        val targetDir = File(File(context.safeStorageContext.filesDir, "themes"), themeId)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        var stylesheetFile: File? = null
        var wallpaperFile: File? = null
        var extensionJsonFile: File? = null

        ZipInputStream(inputStream).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            val buffer = ByteArray(4096)
            while (entry != null) {
                val entryName = entry.name.replace("\\", "/")
                if (!entry.isDirectory) {
                    val cleanFileName = File(entryName).name
                    val outFile = File(targetDir, cleanFileName)
                    FileOutputStream(outFile).use { fos ->
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                        }
                    }

                    if (cleanFileName.equals("stylesheet.json", ignoreCase = true) ||
                        cleanFileName.equals("theme.json", ignoreCase = true)) {
                        stylesheetFile = outFile
                    } else if (cleanFileName.equals("extension.json", ignoreCase = true) ||
                               cleanFileName.equals("manifest.json", ignoreCase = true)) {
                        extensionJsonFile = outFile
                    } else if (cleanFileName.startsWith("background", ignoreCase = true) ||
                               cleanFileName.startsWith("wallpaper", ignoreCase = true) ||
                               cleanFileName.endsWith(".png", ignoreCase = true) ||
                               cleanFileName.endsWith(".jpg", ignoreCase = true) ||
                               cleanFileName.endsWith(".jpeg", ignoreCase = true) ||
                               cleanFileName.endsWith(".webp", ignoreCase = true)) {
                        wallpaperFile = outFile
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        var parsedName = defaultName
        var parsedAuthor = "FlorisBoard User"

        if (extensionJsonFile != null) {
            try {
                val extObj = JSONObject(extensionJsonFile.readText())
                extObj.optString("name").takeIf { it.isNotBlank() }?.let { parsedName = it }
                extObj.optString("author").takeIf { it.isNotBlank() }?.let { parsedAuthor = it }
            } catch (e: Exception) {
                Log.w(TAG, "Could not parse extension.json: ${e.message}")
            }
        }

        val jsonContent = stylesheetFile?.readText()
            ?: extensionJsonFile?.readText()
            ?: throw IllegalArgumentException("No stylesheet.json, theme.json, or extension.json found in package")

        return parseJsonTheme(jsonContent, parsedName, wallpaperFile?.absolutePath).copy(
            id = themeId,
            name = parsedName,
            author = parsedAuthor
        )
    }

    /**
     * Parses FlorisBoard theme JSON into a ParsedFlorisTheme model with resolved variables & CSS rules.
     */
    fun parseJsonTheme(jsonString: String, defaultName: String, wallpaperPath: String?): ParsedFlorisTheme {
        val root = JSONObject(jsonString)

        val id = root.optString("id", "floris_" + UUID.nameUUIDFromBytes(defaultName.toByteArray()).toString().take(8))
        val name = root.optString("name", defaultName)
        val author = root.optString("author", "FlorisBoard User")
        val version = root.optString("version", "1.0.0")
        val isNight = root.optBoolean("isNight", true)

        val savedWallpaper = root.optString("wallpaperPath", "").takeIf { it.isNotBlank() }
        val effectiveWallpaper = wallpaperPath ?: savedWallpaper

        // 1. Extract theme variables
        val variables = HashMap<String, String>()
        val varObj = root.optJSONObject("variables")
        if (varObj != null) {
            val keys = varObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val normalizedKey = if (k.startsWith("$")) k else "$$k"
                variables[normalizedKey] = varObj.optString(k)
                variables[k.removePrefix("$")] = varObj.optString(k)
            }
        }

        // 2. Default initial theme colors
        var keyboardBg = if (isNight) Color.parseColor("#0A195E") else Color.parseColor("#EBF0FA")
        var keyBg = if (isNight) Color.parseColor("#33FFFFFF") else Color.parseColor("#FFFFFFFF")
        var keyBgPressed = if (isNight) Color.parseColor("#55FFFFFF") else Color.parseColor("#D0D8E8")
        var keyText = if (isNight) Color.parseColor("#FFFFFF") else Color.parseColor("#1D2939")
        var accentKeyBg = Color.parseColor("#1D1EFF")
        var accentKeyText = Color.parseColor("#FFFFFF")
        var smartbarBg = if (isNight) Color.parseColor("#142170") else Color.parseColor("#DCE1EB")
        var smartbarText = if (isNight) Color.parseColor("#FFFFFF") else Color.parseColor("#1D2939")
        var strokeColor = if (isNight) Color.parseColor("#22FFFFFF") else Color.parseColor("#D0D5DD")
        var strokeWidth = root.optInt("strokeWidth", 1)
        var cornerRadius = root.optDouble("cornerRadius", 10.0).toFloat()

        // Check direct properties if present in serialized JSON
        resolveColor(root.optString("keyboardBgColor"), variables)?.let { keyboardBg = it }
        resolveColor(root.optString("keyBgColor"), variables)?.let { keyBg = it }
        resolveColor(root.optString("keyBgPressedColor"), variables)?.let { keyBgPressed = it }
        resolveColor(root.optString("keyTextColor"), variables)?.let { keyText = it }
        resolveColor(root.optString("accentKeyBgColor"), variables)?.let { accentKeyBg = it }
        resolveColor(root.optString("accentKeyTextColor"), variables)?.let { accentKeyText = it }
        resolveColor(root.optString("smartbarBgColor"), variables)?.let { smartbarBg = it }
        resolveColor(root.optString("smartbarTextColor"), variables)?.let { smartbarText = it }
        resolveColor(root.optString("strokeColor"), variables)?.let { strokeColor = it }

        // Check top-level variables
        val bgKeys = listOf("\$background", "\$keyboard-background", "\$keyboard-bg", "\$window", "\$bg", "background", "keyboard-background", "keyboard-bg")
        for (k in bgKeys) {
            resolveColor(variables[k], variables)?.let { keyboardBg = it; return@let }
        }

        val surfaceKeys = listOf("\$surface", "\$key-background", "\$key-bg", "\$key", "surface", "key-background", "key-bg")
        for (k in surfaceKeys) {
            resolveColor(variables[k], variables)?.let { keyBg = it; return@let }
        }

        val onSurfaceKeys = listOf("\$on-surface", "\$key-foreground", "\$key-fg", "\$text", "\$on-key", "on-surface", "key-foreground", "key-fg")
        for (k in onSurfaceKeys) {
            resolveColor(variables[k], variables)?.let { keyText = it; return@let }
        }

        val accentKeys = listOf("\$accent", "\$accent-background", "\$accent-bg", "\$primary", "accent", "accent-background", "primary")
        for (k in accentKeys) {
            resolveColor(variables[k], variables)?.let { accentKeyBg = it; return@let }
        }

        val onAccentKeys = listOf("\$on-accent", "\$accent-foreground", "\$accent-fg", "\$on-primary", "on-accent", "accent-foreground")
        for (k in onAccentKeys) {
            resolveColor(variables[k], variables)?.let { accentKeyText = it; return@let }
        }

        val smartbarKeys = listOf("\$smartbar", "\$smartbar-background", "\$smartbar-bg", "\$toolbar", "smartbar", "smartbar-background", "smartbar-bg")
        for (k in smartbarKeys) {
            resolveColor(variables[k], variables)?.let { smartbarBg = it; return@let }
        }

        val outlineKeys = listOf("\$outline", "\$border", "\$stroke", "\$key-border", "outline", "border", "stroke")
        for (k in outlineKeys) {
            resolveColor(variables[k], variables)?.let { strokeColor = it; return@let }
        }

        // 3. Parse Rules / Selectors (JSONArray, JSONObject, or "theme" object)
        fun applyRule(selector: String, style: JSONObject) {
            val s = selector.lowercase()
            when {
                s.contains("window") || s == "root" || s == "keyboard" || s.startsWith("keyboard") -> {
                    resolveColor(style.optString("background", style.optString("bg")), variables)?.let { keyboardBg = it }
                    resolveColor(style.optString("foreground", style.optString("fg", style.optString("text-color"))), variables)?.let { keyText = it }
                }
                s.contains("key[state=pressed]") || s.contains("key:pressed") || s.contains("key[touch]") -> {
                    resolveColor(style.optString("background", style.optString("bg")), variables)?.let { keyBgPressed = it }
                }
                s.contains("accent") || s.contains("enter") || s.contains("action") || s.contains("primary") -> {
                    resolveColor(style.optString("background", style.optString("bg")), variables)?.let { accentKeyBg = it }
                    resolveColor(style.optString("foreground", style.optString("fg", style.optString("text-color"))), variables)?.let { accentKeyText = it }
                }
                s == "key" || s.startsWith("key") || s.contains("key:default") -> {
                    resolveColor(style.optString("background", style.optString("bg")), variables)?.let { keyBg = it }
                    resolveColor(style.optString("foreground", style.optString("fg", style.optString("text-color"))), variables)?.let { keyText = it }
                    resolveColor(style.optString("border-color", style.optString("stroke")), variables)?.let { strokeColor = it }
                    parseRadius(style.optString("shape-radius", style.optString("corner-radius", style.optString("radius"))))?.let { cornerRadius = it }
                    parseBorderWidth(style.optString("border-width", style.optString("stroke-width")))?.let { strokeWidth = it }
                }
                s.contains("smartbar") || s.contains("toolbar") || s.contains("candidate") -> {
                    resolveColor(style.optString("background", style.optString("bg")), variables)?.let { smartbarBg = it }
                    resolveColor(style.optString("foreground", style.optString("fg", style.optString("text-color"))), variables)?.let { smartbarText = it }
                }
            }
        }

        val rulesArray = root.optJSONArray("rules")
        if (rulesArray != null) {
            for (i in 0 until rulesArray.length()) {
                val rule = rulesArray.optJSONObject(i) ?: continue
                val selectors = ArrayList<String>()
                val selArray = rule.optJSONArray("selectors")
                if (selArray != null) {
                    for (j in 0 until selArray.length()) {
                        selectors.add(selArray.optString(j))
                    }
                }
                val selStr = rule.optString("selector", rule.optString("target", ""))
                if (selStr.isNotBlank()) selectors.add(selStr)

                val style = rule.optJSONObject("style") ?: rule.optJSONObject("attributes") ?: rule
                for (sel in selectors) {
                    applyRule(sel, style)
                }
            }
        }

        val rulesMap = root.optJSONObject("rules")
        if (rulesMap != null) {
            val it = rulesMap.keys()
            while (it.hasNext()) {
                val sel = it.next()
                val style = rulesMap.optJSONObject(sel) ?: continue
                applyRule(sel, style)
            }
        }

        val themeMap = root.optJSONObject("theme")
        if (themeMap != null) {
            val it = themeMap.keys()
            while (it.hasNext()) {
                val sel = it.next()
                val style = themeMap.optJSONObject(sel) ?: continue
                applyRule(sel, style)
            }
        }

        return ParsedFlorisTheme(
            id = id,
            name = name,
            author = author,
            version = version,
            isBuiltIn = false,
            isNight = isNight,
            keyboardBgColor = keyboardBg,
            keyBgColor = keyBg,
            keyBgPressedColor = keyBgPressed,
            keyTextColor = keyText,
            accentKeyBgColor = accentKeyBg,
            accentKeyTextColor = accentKeyText,
            smartbarBgColor = smartbarBg,
            smartbarTextColor = smartbarText,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            wallpaperPath = effectiveWallpaper,
            blurBackdrop = effectiveWallpaper != null
        )
    }

    /**
     * Resolves colors from Hex, rgba(), rgb(), or theme variable map.
     */
    fun resolveColor(rawVal: String?, variables: Map<String, String>): Int? {
        if (rawVal.isNullOrBlank()) return null
        val trimmed = rawVal.trim()

        if (trimmed.startsWith("$") || trimmed.startsWith("@")) {
            val varKey = "$" + trimmed.substring(1)
            val mapped = variables[varKey] ?: variables[trimmed.substring(1)]
            if (mapped != null && mapped != rawVal) {
                return resolveColor(mapped, variables)
            }
        }

        return parseColorString(trimmed)
    }

    /**
     * Parses CSS color strings: #RRGGBB, #AARRGGBB, #RGB, #ARGB, rgba(r,g,b,a), rgb(r,g,b).
     */
    fun parseColorString(colorStr: String): Int? {
        return try {
            val s = colorStr.trim().lowercase()
            when {
                s.startsWith("#") -> {
                    when (s.length) {
                        4 -> { // #RGB -> #RRGGBB
                            val r = s[1].toString().repeat(2)
                            val g = s[2].toString().repeat(2)
                            val b = s[3].toString().repeat(2)
                            Color.parseColor("#$r$g$b")
                        }
                        5 -> { // #ARGB -> #AARRGGBB
                            val a = s[1].toString().repeat(2)
                            val r = s[2].toString().repeat(2)
                            val g = s[3].toString().repeat(2)
                            val b = s[4].toString().repeat(2)
                            Color.parseColor("#$a$r$g$b")
                        }
                        7 -> Color.parseColor(s) // #RRGGBB
                        9 -> Color.parseColor(s) // #AARRGGBB
                        else -> null
                    }
                }
                s.startsWith("rgba(") && s.endsWith(")") -> {
                    val parts = s.substring(5, s.length - 1).split(",").map { it.trim() }
                    if (parts.size == 4) {
                        val r = parts[0].toIntOrNull() ?: 0
                        val g = parts[1].toIntOrNull() ?: 0
                        val b = parts[2].toIntOrNull() ?: 0
                        val alphaFloat = parts[3].toFloatOrNull() ?: 1.0f
                        val a = (alphaFloat.coerceIn(0f, 1f) * 255).toInt()
                        Color.argb(a, r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
                    } else null
                }
                s.startsWith("rgb(") && s.endsWith(")") -> {
                    val parts = s.substring(4, s.length - 1).split(",").map { it.trim() }
                    if (parts.size == 3) {
                        val r = parts[0].toIntOrNull() ?: 0
                        val g = parts[1].toIntOrNull() ?: 0
                        val b = parts[2].toIntOrNull() ?: 0
                        Color.rgb(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
                    } else null
                }
                s == "transparent" -> Color.TRANSPARENT
                s == "white" -> Color.WHITE
                s == "black" -> Color.BLACK
                else -> Color.parseColor(s)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseRadius(value: String?): Float? {
        if (value.isNullOrBlank()) return null
        val digits = value.replace("dp", "").replace("px", "").trim()
        return digits.toFloatOrNull()
    }

    private fun parseBorderWidth(value: String?): Int? {
        if (value.isNullOrBlank()) return null
        val digits = value.replace("dp", "").replace("px", "").trim()
        return digits.toIntOrNull()
    }
}
