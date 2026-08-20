package org.fossify.keyboard.helpers

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import org.fossify.keyboard.nlp.WordPredictionEngine

/**
 * Bridge connecting FlorisBoard dynamic key events with Sinhala Singlish (Phonetic)
 * Transliteration, Wijesekara mapping, and word prediction engines.
 */
class SinglishCompatBridge(
    private val sinhalaIME: SinhalaIME
) {

    fun handleKeyEvent(
        inputConnection: InputConnection?,
        editorInfo: EditorInfo?,
        code: Int,
        label: String,
        isLongPress: Boolean,
        currentLanguage: Int,
        onSwitchLanguage: (() -> Unit)? = null,
        onOpenSettings: (() -> Unit)? = null,
        onOpenEmoji: (() -> Unit)? = null
    ) {
        if (inputConnection == null) return

        when (code) {
            MyKeyboard.KEYCODE_DELETE -> {
                if (currentLanguage == LANGUAGE_SINHALA_SINGLISH && sinhalaIME.isComposing) {
                    sinhalaIME.onBackspace(inputConnection)
                } else {
                    inputConnection.deleteSurroundingText(1, 0)
                }
            }
            MyKeyboard.KEYCODE_ENTER -> {
                if (currentLanguage == LANGUAGE_SINHALA_SINGLISH && sinhalaIME.isComposing) {
                    sinhalaIME.finishComposing(inputConnection)
                }
                val imeOptions = editorInfo?.imeOptions ?: EditorInfo.IME_NULL
                val actionId = imeOptions and EditorInfo.IME_MASK_ACTION
                if (actionId != EditorInfo.IME_ACTION_NONE && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
                    inputConnection.performEditorAction(actionId)
                } else {
                    inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }
            }
            MyKeyboard.KEYCODE_SPACE -> {
                if (currentLanguage == LANGUAGE_SINHALA_SINGLISH && sinhalaIME.isComposing) {
                    sinhalaIME.finishComposing(inputConnection, autoCorrect = true)
                }
                inputConnection.commitText(" ", 1)
            }
            MyKeyboard.KEYCODE_MODE_CHANGE -> {
                onSwitchLanguage?.invoke()
            }
            MyKeyboard.KEYCODE_POPUP_SETTINGS -> {
                onOpenSettings?.invoke()
            }
            MyKeyboard.KEYCODE_POPUP_EMOJI -> {
                onOpenEmoji?.invoke()
            }
            else -> {
                val textToInsert = if (label.isNotEmpty()) label else if (code > 0) code.toChar().toString() else ""
                if (textToInsert.isNotEmpty()) {
                    if (currentLanguage == LANGUAGE_SINHALA_SINGLISH && textToInsert.length == 1 && (Character.isLetter(textToInsert[0]) || textToInsert == "~" || textToInsert == "X" || textToInsert == "x" || textToInsert == "H")) {
                        val handled = sinhalaIME.onInputCharacter(inputConnection, textToInsert[0])
                        if (!handled) {
                            inputConnection.commitText(textToInsert, 1)
                        }
                    } else if (currentLanguage == LANGUAGE_SINHALA_WIJESEKARA && textToInsert.length == 1) {
                        val mapped = WijesekaraEngine.getSinhalaChar(textToInsert[0])
                        inputConnection.commitText(mapped, 1)
                    } else {
                        if (sinhalaIME.isComposing) {
                            sinhalaIME.finishComposing(inputConnection)
                        }
                        inputConnection.commitText(textToInsert, 1)
                    }
                }
            }
        }
    }
}
