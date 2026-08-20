package org.fossify.keyboard.helpers

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.util.Log
import android.util.TypedValue
import org.fossify.keyboard.extensions.config
import org.fossify.keyboard.extensions.safeStorageContext
import org.fossify.keyboard.models.ParsedFlorisTheme
import org.json.JSONObject
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

object ThemeEngine {

    private const val TAG = "ThemeEngine"
    private const val PREFS_KEY_ACTIVE_THEME = "active_theme_id"
    private const val THEMES_DIR = "themes"
    private const val PREFS_NAME = "lakmal_keyboard_prefs"

    val THEME_DEFAULT_DARK = "builtin_default_dark"

    private val themeListeners = CopyOnWriteArrayList<(ParsedFlorisTheme) -> Unit>()
    private var cachedActiveTheme: ParsedFlorisTheme? = null

    fun addListener(listener: (ParsedFlorisTheme) -> Unit) {
        if (!themeListeners.contains(listener)) {
            themeListeners.add(listener)
        }
    }

    fun removeListener(listener: (ParsedFlorisTheme) -> Unit) {
        themeListeners.remove(listener)
    }

    fun addThemeChangeListener(listener: (ParsedFlorisTheme) -> Unit) {
        addListener(listener)
    }

    fun removeThemeChangeListener(listener: (ParsedFlorisTheme) -> Unit) {
        removeListener(listener)
    }

    val builtInThemes: List<ParsedFlorisTheme> = listOf(
        ParsedFlorisTheme(
            id = "builtin_default_dark",
            name = "Flashboard Default",
            author = "Flashboard",
            isBuiltIn = true,
            isNight = true,
            keyboardBgColor = Color.parseColor("#131316"),
            keyBgColor = Color.parseColor("#23242A"),
            keyBgPressedColor = Color.parseColor("#373942"),
            keyTextColor = Color.parseColor("#FFFFFF"),
            accentKeyBgColor = Color.parseColor("#0066FF"),
            accentKeyTextColor = Color.parseColor("#FFFFFF"),
            smartbarBgColor = Color.parseColor("#1B1C22"),
            smartbarTextColor = Color.parseColor("#FFFFFF"),
            strokeColor = Color.parseColor("#22FFFFFF"),
            strokeWidth = 1,
            cornerRadius = 10f
        )
    )

    private fun getThemesDir(context: Context): File {
        val dir = File(context.safeStorageContext.filesDir, THEMES_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getActiveTheme(context: Context): ParsedFlorisTheme {
        cachedActiveTheme?.let { return it }
        val prefs = context.safeStorageContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val activeId = prefs.getString(PREFS_KEY_ACTIVE_THEME, THEME_DEFAULT_DARK) ?: THEME_DEFAULT_DARK

        val theme = builtInThemes.find { it.id == activeId }
            ?: getCustomThemeById(context, activeId)
            ?: builtInThemes.first()

        cachedActiveTheme = theme
        return theme
    }

    fun setActiveTheme(context: Context, themeId: String) {
        context.safeStorageContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(PREFS_KEY_ACTIVE_THEME, themeId)
            .commit()

        cachedActiveTheme = null
        val newTheme = getActiveTheme(context)

        // Notify in-process listeners
        themeListeners.forEach { it.invoke(newTheme) }
    }

    fun getAllThemes(context: Context): List<ParsedFlorisTheme> {
        val list = ArrayList<ParsedFlorisTheme>()
        list.addAll(builtInThemes)
        list.addAll(getCustomThemes(context))
        return list
    }

    fun getCustomThemes(context: Context): List<ParsedFlorisTheme> {
        val list = ArrayList<ParsedFlorisTheme>()
        val dir = getThemesDir(context)
        val files = dir.listFiles { f -> f.isFile && f.extension == "json" } ?: return emptyList()

        for (file in files) {
            try {
                val content = file.readText()
                val theme = FlorisThemeParser.parseJsonTheme(content, file.nameWithoutExtension, null)
                list.add(theme)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read custom theme file ${file.name}: ${e.message}")
            }
        }
        return list
    }

    fun getCustomThemeById(context: Context, id: String): ParsedFlorisTheme? {
        val file = File(getThemesDir(context), "$id.json")
        if (!file.exists()) return null
        return try {
            FlorisThemeParser.parseJsonTheme(file.readText(), file.nameWithoutExtension, null)
        } catch (e: Exception) {
            null
        }
    }

    fun saveCustomTheme(context: Context, theme: ParsedFlorisTheme) {
        try {
            val file = File(getThemesDir(context), "${theme.id}.json")
            val json = serializeTheme(theme)
            file.writeText(json.toString(2))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save theme ${theme.name}: ${e.message}", e)
        }
    }

    fun deleteCustomTheme(context: Context, themeId: String): Boolean {
        val file = File(getThemesDir(context), "$themeId.json")
        val deleted = file.delete()
        if (getActiveTheme(context).id == themeId) {
            setActiveTheme(context, THEME_DEFAULT_DARK)
        }
        return deleted
    }

    fun getDynamicKeyDrawable(
        isAction: Boolean
    ): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 14f
        val color = if (isAction) Color.parseColor("#1D1EFF") else Color.parseColor("#33FFFFFF")
        drawable.setColor(color)
        drawable.setStroke(2, Color.parseColor("#22FFFFFF"))
        return drawable
    }

    /**
     * Constructs a dynamic background drawable for the keyboard backplate.
     * Supports image wallpapers with frosted dark/light glass scrim, or solid/gradient background.
     */
    fun getKeyboardBackground(context: Context, theme: ParsedFlorisTheme = getActiveTheme(context)): Drawable {
        if (!theme.wallpaperPath.isNullOrEmpty()) {
            val imageFile = File(theme.wallpaperPath)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (bitmap != null) {
                    val bitmapDrawable = BitmapDrawable(context.resources, bitmap)
                    val scrimColor = if (theme.isNight) Color.parseColor("#990A195E") else Color.parseColor("#99FFFFFF")
                    val scrimDrawable = ColorDrawable(scrimColor)
                    return LayerDrawable(arrayOf(bitmapDrawable, scrimDrawable))
                }
            }
        }

        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.setColor(theme.keyboardBgColor)
        return drawable
    }

    /**
     * Constructs a stateful Ripple / Gradient Drawable for standard keys or action keys.
     */
    fun getKeyDrawable(
        context: Context,
        isAction: Boolean,
        theme: ParsedFlorisTheme = getActiveTheme(context)
    ): Drawable {
        val config = context.config
        val dpRadius = dpToPx(context, theme.cornerRadius)
        
        val isBorderEnabled = config.keyBordersEnabled
        val baseStrokeWidth = if (isBorderEnabled) config.keyBorderWidth else 0f
        val dpStroke = dpToPx(context, baseStrokeWidth).toInt()

        val normalColor = if (isAction) theme.accentKeyBgColor else theme.keyBgColor
        val pressedColor = if (isAction) {
            Color.argb(
                220,
                Color.red(theme.accentKeyBgColor),
                Color.green(theme.accentKeyBgColor),
                Color.blue(theme.accentKeyBgColor)
            )
        } else {
            theme.keyBgPressedColor
        }

        val shape = GradientDrawable().apply {
            this.shape = GradientDrawable.RECTANGLE
            cornerRadius = dpRadius
            setColor(normalColor)
            if (dpStroke > 0) {
                setStroke(dpStroke, theme.strokeColor)
            } else {
                setStroke(0, Color.TRANSPARENT)
            }
        }

        val mask = GradientDrawable().apply {
            this.shape = GradientDrawable.RECTANGLE
            cornerRadius = dpRadius
            setColor(Color.WHITE)
        }

        return RippleDrawable(ColorStateList.valueOf(pressedColor), shape, mask)
    }

    /**
     * Constructs Smartbar / Toolbar background drawable.
     */
    fun getSmartbarBackground(
        context: Context,
        theme: ParsedFlorisTheme = getActiveTheme(context)
    ): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.setColor(theme.smartbarBgColor)
        return drawable
    }

    /**
     * Constructs candidate word chip background drawable.
     */
    fun getCandidateChipBackground(
        context: Context,
        isPrimary: Boolean,
        theme: ParsedFlorisTheme = getActiveTheme(context)
    ): Drawable {
        val dpRadius = dpToPx(context, 16f)
        val dpStroke = dpToPx(context, 1f).toInt()
        val bg = if (isPrimary) theme.accentKeyBgColor else theme.keyBgColor
        val stroke = if (isPrimary) Color.TRANSPARENT else theme.strokeColor

        val shape = GradientDrawable().apply {
            this.shape = GradientDrawable.RECTANGLE
            cornerRadius = dpRadius
            setColor(bg)
            if (dpStroke > 0) {
                setStroke(dpStroke, stroke)
            }
        }
        val mask = GradientDrawable().apply {
            this.shape = GradientDrawable.RECTANGLE
            cornerRadius = dpRadius
            setColor(Color.WHITE)
        }
        return RippleDrawable(ColorStateList.valueOf(theme.keyBgPressedColor), shape, mask)
    }

    /**
     * Constructs recent emoji pill chip drawable.
     */
    fun getRecentEmojiChipBackground(
        context: Context,
        theme: ParsedFlorisTheme = getActiveTheme(context)
    ): Drawable {
        val dpRadius = dpToPx(context, 14f)
        val shape = GradientDrawable().apply {
            this.shape = GradientDrawable.RECTANGLE
            cornerRadius = dpRadius
            setColor(theme.keyBgColor)
            setStroke(dpToPx(context, 1f).toInt(), theme.strokeColor)
        }
        val mask = GradientDrawable().apply {
            this.shape = GradientDrawable.RECTANGLE
            cornerRadius = dpRadius
            setColor(Color.WHITE)
        }
        return RippleDrawable(ColorStateList.valueOf(theme.keyBgPressedColor), shape, mask)
    }

    private fun serializeTheme(theme: ParsedFlorisTheme): JSONObject {
        val root = JSONObject()
        root.put("id", theme.id)
        root.put("name", theme.name)
        root.put("author", theme.author)
        root.put("version", theme.version)
        root.put("isNight", theme.isNight)
        root.put("wallpaperPath", theme.wallpaperPath ?: "")
        root.put("cornerRadius", theme.cornerRadius.toDouble())
        root.put("strokeWidth", theme.strokeWidth)

        root.put("keyboardBgColor", String.format("#%08X", theme.keyboardBgColor))
        root.put("keyBgColor", String.format("#%08X", theme.keyBgColor))
        root.put("keyBgPressedColor", String.format("#%08X", theme.keyBgPressedColor))
        root.put("keyTextColor", String.format("#%08X", theme.keyTextColor))
        root.put("accentKeyBgColor", String.format("#%08X", theme.accentKeyBgColor))
        root.put("accentKeyTextColor", String.format("#%08X", theme.accentKeyTextColor))
        root.put("smartbarBgColor", String.format("#%08X", theme.smartbarBgColor))
        root.put("smartbarTextColor", String.format("#%08X", theme.smartbarTextColor))
        root.put("strokeColor", String.format("#%08X", theme.strokeColor))

        val variables = JSONObject()
        variables.put("\$background", String.format("#%08X", theme.keyboardBgColor))
        variables.put("\$surface", String.format("#%08X", theme.keyBgColor))
        variables.put("\$on-surface", String.format("#%08X", theme.keyTextColor))
        variables.put("\$accent", String.format("#%08X", theme.accentKeyBgColor))
        variables.put("\$on-accent", String.format("#%08X", theme.accentKeyTextColor))
        variables.put("\$smartbar", String.format("#%08X", theme.smartbarBgColor))
        variables.put("\$outline", String.format("#%08X", theme.strokeColor))
        root.put("variables", variables)

        return root
    }

    private fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }
}
