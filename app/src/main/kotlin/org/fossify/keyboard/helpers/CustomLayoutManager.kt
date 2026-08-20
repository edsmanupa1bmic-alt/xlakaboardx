package org.fossify.keyboard.helpers

import android.content.Context
import android.net.Uri
import android.util.Log
import org.fossify.keyboard.R
import org.fossify.keyboard.extensions.config
import org.fossify.keyboard.extensions.safeStorageContext
import org.fossify.keyboard.models.CustomKeyboardLayout
import org.fossify.keyboard.models.CustomKeyDef
import org.fossify.keyboard.models.CustomRowDef
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt

object CustomLayoutManager {

    private const val TAG = "CustomLayoutManager"
    private const val LAYOUTS_DIR = "layouts"
    private const val PREFS_KEY_ACTIVE_LAYOUT = "active_custom_layout_id"

    private val layoutListeners = CopyOnWriteArrayList<() -> Unit>()

    fun addListener(listener: () -> Unit) {
        layoutListeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        layoutListeners.remove(listener)
    }

    private fun getLayoutsDir(context: Context): File {
        val dir = File(context.safeStorageContext.filesDir, LAYOUTS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Imports a layout from a Content URI (from SAF file picker).
     */
    fun importLayoutFromUri(context: Context, uri: Uri): Result<CustomKeyboardLayout> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                InputStreamReader(stream).readText()
            } ?: throw IllegalArgumentException("Could not open file stream")

            var fileName = "Imported Layout"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex).substringBeforeLast(".")
                    }
                }
            }

            val parsedLayout = FlorisLayoutParser.parseLayout(content, fileName)
            saveLayout(context, parsedLayout)
            Result.success(parsedLayout)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import layout from URI: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Saves a CustomKeyboardLayout to disk as JSON.
     */
    fun saveLayout(context: Context, layout: CustomKeyboardLayout) {
        try {
            val file = File(getLayoutsDir(context), "${layout.id}.json")
            val json = serializeLayout(layout)
            file.writeText(json.toString(2))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save layout: ${e.message}", e)
        }
    }

    /**
     * Retrieves all saved custom layouts.
     */
    fun getSavedLayouts(context: Context): List<CustomKeyboardLayout> {
        val list = ArrayList<CustomKeyboardLayout>()
        val dir = getLayoutsDir(context)
        val files = dir.listFiles { f -> f.extension == "json" } ?: return emptyList()

        for (file in files) {
            try {
                val content = file.readText()
                val layout = FlorisLayoutParser.parseLayout(content, file.nameWithoutExtension)
                list.add(layout)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading layout file ${file.name}: ${e.message}")
            }
        }
        return list
    }

    fun getLayoutById(context: Context, layoutId: String): CustomKeyboardLayout? {
        val file = File(getLayoutsDir(context), "$layoutId.json")
        if (!file.exists()) return null
        return try {
            FlorisLayoutParser.parseLayout(file.readText(), file.nameWithoutExtension)
        } catch (e: Exception) {
            null
        }
    }

    fun deleteLayout(context: Context, layoutId: String): Boolean {
        val file = File(getLayoutsDir(context), "$layoutId.json")
        val deleted = file.delete()
        if (getActiveLayoutId(context) == layoutId) {
            setActiveLayoutId(context, null)
        }
        return deleted
    }

    fun getActiveLayoutId(context: Context): String? {
        return context.safeStorageContext.getSharedPreferences("lakmal_keyboard_prefs", Context.MODE_PRIVATE)
            .getString(PREFS_KEY_ACTIVE_LAYOUT, null)
    }

    fun getActiveLayout(context: Context): CustomKeyboardLayout? {
        val activeId = getActiveLayoutId(context) ?: return null
        return getLayoutById(context, activeId)
    }

    fun setActiveLayoutId(context: Context, layoutId: String?) {
        context.safeStorageContext.getSharedPreferences("lakmal_keyboard_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(PREFS_KEY_ACTIVE_LAYOUT, layoutId)
            .commit()

        // Notify listeners
        layoutListeners.forEach { it.invoke() }
    }

    /**
     * Serializes a layout object to JSONObject.
     */
    private fun serializeLayout(layout: CustomKeyboardLayout): JSONObject {
        val root = JSONObject()
        root.put("id", layout.id)
        root.put("name", layout.name)
        root.put("label", layout.label)
        root.put("direction", layout.direction)

        val arrangement = JSONArray()
        for (row in layout.rows) {
            val rowArr = JSONArray()
            for (key in row.keys) {
                val keyObj = JSONObject()
                keyObj.put("type", key.type)
                keyObj.put("code", key.code)
                keyObj.put("label", key.label)
                keyObj.put("weight", key.widthWeight.toDouble())
                if (key.topSmallNumber.isNotEmpty()) {
                    keyObj.put("topSmallNumber", key.topSmallNumber)
                }
                if (key.popupCharacters.isNotEmpty()) {
                    val popupArr = JSONArray()
                    key.popupCharacters.forEach { popupArr.put(it) }
                    keyObj.put("popup", popupArr)
                }
                rowArr.put(keyObj)
            }
            arrangement.put(rowArr)
        }
        root.put("arrangement", arrangement)
        return root
    }

    /**
     * Dynamically builds a MyKeyboard object from a CustomKeyboardLayout definition.
     */
    fun buildKeyboard(
        context: Context,
        layout: CustomKeyboardLayout,
        enterKeyType: Int
    ): MyKeyboard {
        // We use the empty constructor pattern and populate MyKeyboard rows and keys
        val displayWidth = context.resources.displayMetrics.widthPixels
        val keyboard = MyKeyboard(context, R.xml.keys_letters_english_qwerty, enterKeyType)
        
        // Clear and rebuild keys dynamically
        keyboard.mKeys?.clear()
        val keyList = ArrayList<MyKeyboard.Key>()
        val rowHeight = (context.resources.getDimension(R.dimen.key_height) * keyboard.mKeyboardHeightMultiplier).roundToInt()
        var currentY = 0

        for (rowDef in layout.rows) {
            val row = MyKeyboard.Row(keyboard)
            row.defaultHeight = rowHeight
            row.defaultHorizontalGap = 0
            row.isNumbersRow = rowDef.isNumbersRow

            val totalWeight = rowDef.keys.map { it.widthWeight }.sum().coerceAtLeast(1.0f)
            var currentX = 0

            for (keyDef in rowDef.keys) {
                val keyWidth = ((keyDef.widthWeight / totalWeight) * displayWidth).roundToInt()
                val key = MyKeyboard.Key(row)
                key.x = currentX
                key.y = currentY
                key.width = keyWidth
                key.height = rowHeight
                key.gap = 0
                key.label = keyDef.label
                key.code = keyDef.code
                key.topSmallNumber = keyDef.topSmallNumber

                if (keyDef.popupCharacters.isNotEmpty()) {
                    key.popupCharacters = keyDef.popupCharacters.joinToString("")
                }

                if (keyDef.code == MyKeyboard.KEYCODE_ENTER) {
                    key.icon = context.resources.getDrawable(R.drawable.ic_enter_vector, context.theme)
                } else if (keyDef.code == MyKeyboard.KEYCODE_DELETE) {
                    key.icon = context.resources.getDrawable(R.drawable.ic_delete_vector, context.theme)
                } else if (keyDef.code == MyKeyboard.KEYCODE_SHIFT) {
                    key.icon = context.resources.getDrawable(R.drawable.ic_caps_outline_vector, context.theme)
                }

                keyList.add(key)
                row.mKeys.add(key)
                currentX += keyWidth
            }
            currentY += rowHeight
        }

        keyboard.mKeys = keyList
        keyboard.mHeight = currentY
        keyboard.mMinWidth = displayWidth
        return keyboard
    }
}
