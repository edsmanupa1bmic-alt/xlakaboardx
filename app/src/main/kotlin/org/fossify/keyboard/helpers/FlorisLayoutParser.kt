package org.fossify.keyboard.helpers

import android.util.Log
import org.fossify.keyboard.models.CustomKeyboardLayout
import org.fossify.keyboard.models.CustomKeyDef
import org.fossify.keyboard.models.CustomRowDef
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object FlorisLayoutParser {

    private const val TAG = "FlorisLayoutParser"

    /**
     * Parses a FlorisBoard layout JSON string into a structured CustomKeyboardLayout.
     * Supports both standard FlorisBoard arrangement schema and simplified row arrays.
     */
    fun parseLayout(jsonString: String, defaultName: String? = null): CustomKeyboardLayout {
        return try {
            val root = JSONObject(jsonString)
            val name = root.optString("name", defaultName ?: "Custom Floris Layout")
            val label = root.optString("label", name.take(8))
            val direction = root.optString("direction", "ltr")
            val id = root.optString("id", "floris_" + UUID.nameUUIDFromBytes(name.toByteArray()).toString().take(8))

            val rowsList = ArrayList<CustomRowDef>()
            val arrangement = root.optJSONArray("arrangement") ?: root.optJSONArray("rows")

            if (arrangement != null) {
                for (rowIndex in 0 until arrangement.length()) {
                    val rowArray = arrangement.optJSONArray(rowIndex)
                    if (rowArray != null) {
                        val keyList = ArrayList<CustomKeyDef>()
                        for (keyIndex in 0 until rowArray.length()) {
                            val keyObj = rowArray.optJSONObject(keyIndex)
                            if (keyObj != null) {
                                keyList.add(parseKeyObject(keyObj))
                            }
                        }
                        if (keyList.isNotEmpty()) {
                            rowsList.add(CustomRowDef(keys = keyList))
                        }
                    }
                }
            }

            if (rowsList.isEmpty()) {
                fallbackDefaultLayout(name)
            } else {
                CustomKeyboardLayout(
                    id = id,
                    name = name,
                    label = label,
                    direction = direction,
                    rows = rowsList
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Floris layout JSON: ${e.message}", e)
            fallbackDefaultLayout(defaultName ?: "Imported Layout")
        }
    }

    private fun parseKeyObject(keyObj: JSONObject): CustomKeyDef {
        val rawType = keyObj.optString("type", "character").lowercase()
        var label = keyObj.optString("label", "")
        var code = keyObj.optInt("code", 0)
        val weight = keyObj.optDouble("weight", 1.0).toFloat().coerceIn(0.5f, 4.0f)
        val topNumber = keyObj.optString("topSmallNumber", "")

        // Map FlorisBoard key types to internal KeyCodes
        val normalizedType = when {
            rawType == "space" || rawType == "space_bar" -> {
                code = MyKeyboard.KEYCODE_SPACE
                if (label.isEmpty()) label = "Space"
                "space"
            }
            rawType == "enter" || rawType == "action" -> {
                code = MyKeyboard.KEYCODE_ENTER
                if (label.isEmpty()) label = "Enter"
                "enter"
            }
            rawType == "delete" || rawType == "backspace" -> {
                code = MyKeyboard.KEYCODE_DELETE
                if (label.isEmpty()) label = "Del"
                "delete"
            }
            rawType == "shift" || rawType == "caps" -> {
                code = MyKeyboard.KEYCODE_SHIFT
                if (label.isEmpty()) label = "Shift"
                "shift"
            }
            rawType == "symbols_mode_change" || rawType == "symbols" || rawType == "symbol" -> {
                code = MyKeyboard.KEYCODE_SYMBOLS_MODE_CHANGE
                if (label.isEmpty()) label = "?123"
                "symbols_mode_change"
            }
            rawType == "mode_change" || rawType == "switch" -> {
                code = MyKeyboard.KEYCODE_MODE_CHANGE
                if (label.isEmpty()) label = "ABC"
                "mode_change"
            }
            rawType == "emoji" || rawType == "emojis" -> {
                code = MyKeyboard.KEYCODE_EMOJI_OR_LANGUAGE
                if (label.isEmpty()) label = "😊"
                "emoji"
            }
            else -> {
                if (label.isNotEmpty() && code == 0) {
                    code = label.first().code
                }
                "character"
            }
        }

        // Parse secondary popup characters
        val popups = ArrayList<String>()
        val popupObj = keyObj.opt("popup")
        when (popupObj) {
            is JSONArray -> {
                for (p in 0 until popupObj.length()) {
                    val pItem = popupObj.opt(p)
                    if (pItem is JSONObject) {
                        val pLabel = pItem.optString("label", "")
                        if (pLabel.isNotEmpty()) popups.add(pLabel)
                    } else if (pItem is String && pItem.isNotEmpty()) {
                        popups.add(pItem)
                    }
                }
            }
            is JSONObject -> {
                val main = popupObj.optJSONObject("main")?.optString("label")
                if (!main.isNullOrEmpty()) popups.add(main)
                val relevant = popupObj.optJSONArray("relevant")
                if (relevant != null) {
                    for (r in 0 until relevant.length()) {
                        val item = relevant.optJSONObject(r)?.optString("label") ?: relevant.optString(r)
                        if (item.isNotEmpty()) popups.add(item)
                    }
                }
            }
            is String -> {
                if (popupObj.isNotEmpty()) {
                    popupObj.forEach { popups.add(it.toString()) }
                }
            }
        }

        return CustomKeyDef(
            type = normalizedType,
            code = code,
            label = label,
            popupCharacters = popups,
            widthWeight = weight,
            topSmallNumber = topNumber
        )
    }

    private fun fallbackDefaultLayout(name: String): CustomKeyboardLayout {
        // Fallback QWERTY with Sinhala popups
        val row1 = "qwertyuiop".mapIndexed { i, c ->
            CustomKeyDef(
                type = "character",
                code = c.code,
                label = c.toString(),
                popupCharacters = listOf("${i + 1}"),
                topSmallNumber = "${(i + 1) % 10}"
            )
        }
        val row2 = "asdfghjkl".map { c ->
            CustomKeyDef(type = "character", code = c.code, label = c.toString())
        }
        val row3 = listOf(
            CustomKeyDef(type = "shift", code = MyKeyboard.KEYCODE_SHIFT, label = "Shift", widthWeight = 1.5f)
        ) + "zxcvbnm".map { c ->
            CustomKeyDef(type = "character", code = c.code, label = c.toString())
        } + listOf(
            CustomKeyDef(type = "delete", code = MyKeyboard.KEYCODE_DELETE, label = "Del", widthWeight = 1.5f)
        )
        val row4 = listOf(
            CustomKeyDef(type = "symbols_mode_change", code = MyKeyboard.KEYCODE_SYMBOLS_MODE_CHANGE, label = "?123", widthWeight = 1.5f),
            CustomKeyDef(type = "emoji", code = MyKeyboard.KEYCODE_EMOJI_OR_LANGUAGE, label = "😊", widthWeight = 1.0f),
            CustomKeyDef(type = "space", code = MyKeyboard.KEYCODE_SPACE, label = "Space", widthWeight = 5.0f),
            CustomKeyDef(type = "enter", code = MyKeyboard.KEYCODE_ENTER, label = "Enter", widthWeight = 2.0f)
        )

        return CustomKeyboardLayout(
            id = "floris_fallback",
            name = name,
            label = name.take(8),
            rows = listOf(
                CustomRowDef(keys = row1),
                CustomRowDef(keys = row2),
                CustomRowDef(keys = row3),
                CustomRowDef(keys = row4)
            )
        )
    }
}
