package org.fossify.keyboard.helpers

import android.view.inputmethod.InputConnection
import org.fossify.keyboard.nlp.SuggestionItem
import org.fossify.keyboard.nlp.WordPredictionEngine

/**
 * Sinhala Singlish phonetic transliteration engine and predictive typing manager.
 * Emits state hooks for real-time Dynamic Top Bar switching between the Smartbar shortcuts
 * (Idle state) and horizontal suggestion candidate strip (Typing state).
 */
class SinhalaIME {

    private val rawBuffer = StringBuilder()

    var onSuggestionsChanged: ((List<SuggestionItem>) -> Unit)? = null
    var onTypingStateChanged: ((isTyping: Boolean) -> Unit)? = null

    val isComposing: Boolean
        get() = rawBuffer.isNotEmpty()

    val bufferText: String
        get() = rawBuffer.toString()

    /**
     * Handles typing a character in Singlish mode.
     * Updates the composing buffer, sets composing text in InputConnection,
     * fires onTypingStateChanged(true), and queries WordPredictionEngine.
     */
    fun onInputCharacter(inputConnection: InputConnection, char: Char): Boolean {
        if (Character.isLetter(char) || char == '~' || char == 'X' || char == 'x' || char == 'H') {
            val wasEmpty = rawBuffer.isEmpty()
            rawBuffer.append(char)
            val sinhalaText = SinglishParser.parse(rawBuffer.toString())
            inputConnection.setComposingText(sinhalaText, 1)
            
            if (wasEmpty) {
                onTypingStateChanged?.invoke(true)
            }
            updateSuggestions()
            return true
        } else {
            finishComposing(inputConnection)
            return false
        }
    }

    /**
     * Handles typing English characters for word prediction suggestions without phonetic transliteration.
     */
    fun onInputEnglishCharacter(inputConnection: InputConnection, char: Char): Boolean {
        if (Character.isLetter(char) || char == '\'') {
            val wasEmpty = rawBuffer.isEmpty()
            rawBuffer.append(char)
            if (wasEmpty) {
                onTypingStateChanged?.invoke(true)
            }
            val suggestions = WordPredictionEngine.getInstance().getSuggestions(rawBuffer.toString(), isSinglishMode = false)
            onSuggestionsChanged?.invoke(suggestions)
            return true
        } else {
            finishComposing(inputConnection)
            return false
        }
    }

    /**
     * Smart Backspace:
     * Steps back one phonetic modification within the active composing buffer.
     * E.g.: 'කා' -> 'ක' -> 'ක්' -> empty
     * Returns true if backspace was handled in composing buffer, false otherwise.
     */
    fun onBackspace(inputConnection: InputConnection): Boolean {
        if (rawBuffer.isNotEmpty()) {
            rawBuffer.deleteCharAt(rawBuffer.length - 1)
            if (rawBuffer.isEmpty()) {
                inputConnection.setComposingText("", 1)
                inputConnection.finishComposingText()
                clearSuggestions()
                onTypingStateChanged?.invoke(false)
            } else {
                val sinhalaText = SinglishParser.parse(rawBuffer.toString())
                inputConnection.setComposingText(sinhalaText, 1)
                updateSuggestions()
            }
            return true
        }
        return false
    }

    /**
     * Handles user tapping on a candidate suggestion chip.
     * Replaces active composing text with the selected suggestion,
     * finishes composing, appends a space, and registers word in user dictionary.
     */
    fun onSelectSuggestion(inputConnection: InputConnection, item: SuggestionItem) {
        // Commit selected suggestion text
        inputConnection.commitText(item.text, 1)
        // Automatically append space
        inputConnection.commitText(" ", 1)
        inputConnection.finishComposingText()

        // Learn the committed word in user dictionary
        WordPredictionEngine.getInstance().learnWord(item.text, item.isSinhala)

        rawBuffer.clear()
        clearSuggestions()
        onTypingStateChanged?.invoke(false)
    }

    /**
     * Handles emoji tap: finishes active composing word if any, commits the emoji, and clears suggestions.
     */
    fun onCommitEmoji(inputConnection: InputConnection, emoji: String) {
        finishComposing(inputConnection)
        inputConnection.commitText(emoji, 1)
        clearSuggestions()
        onTypingStateChanged?.invoke(false)
    }

    /**
     * Finishes and commits the active composing word.
     * Called on Spacebar, Punctuation, Enter, Mode Switch, or Focus Change.
     * If autoCorrect is enabled, checks for smart correction fixes before committing.
     */
    fun finishComposing(inputConnection: InputConnection?, autoCorrect: Boolean = false) {
        if (rawBuffer.isNotEmpty()) {
            val raw = rawBuffer.toString()
            val parsedText = SinglishParser.parse(raw)
            val textToCommit = if (autoCorrect) {
                WordPredictionEngine.getInstance().getAutoCorrection(raw, isSinglish = true) ?: parsedText
            } else {
                parsedText
            }

            if (textToCommit != parsedText) {
                inputConnection?.commitText(textToCommit, 1)
            }
            inputConnection?.finishComposingText()

            if (textToCommit.isNotBlank()) {
                WordPredictionEngine.getInstance().learnWord(textToCommit, true)
            }
            rawBuffer.clear()
            clearSuggestions()
            onTypingStateChanged?.invoke(false)
        }
    }

    /**
     * Clears internal buffer without committing and reverts top bar to idle state.
     */
    fun reset() {
        rawBuffer.clear()
        clearSuggestions()
        onTypingStateChanged?.invoke(false)
    }

    private fun updateSuggestions() {
        if (rawBuffer.isEmpty()) {
            clearSuggestions()
            onTypingStateChanged?.invoke(false)
            return
        }
        val suggestions = WordPredictionEngine.getInstance().getSuggestions(rawBuffer.toString(), isSinglishMode = true)
        onSuggestionsChanged?.invoke(suggestions)
    }

    private fun clearSuggestions() {
        onSuggestionsChanged?.invoke(emptyList())
    }

    /**
     * EXPERIMENTAL: Flashboard View Binding Hook
     * As requested, this represents the integration point for binding the Flashboard 100% matched XML Layout.
     * Note: In a production Service, `binding` would reside in `SimpleKeyboardIME`, but this hook handles
     * the view interactions and dynamic switching for the `KeyboardRootBinding`.
     */
    fun bindFlashboardKeyboardRoot(binding: org.fossify.keyboard.databinding.KeyboardRootBinding, context: android.content.Context) {
        val config = org.fossify.keyboard.helpers.Config.newInstance(context)
        
        if (config.bgImageEnabled) {
            val bgFile = java.io.File(context.filesDir, "themes/custom_bg.jpg")
            if (bgFile.exists()) {
                binding.keyboardBgImage.visibility = android.view.View.VISIBLE
                binding.keyboardBgDim.visibility = android.view.View.VISIBLE
                binding.keyboardBgImage.setImageBitmap(android.graphics.BitmapFactory.decodeFile(bgFile.absolutePath))
                binding.keyboardBgDim.alpha = config.bgDimOpacity
            }
        }

         
        
        onTypingStateChanged = { isTyping ->
            if (isTyping) {
                binding.flashboardUtilityBar.visibility = android.view.View.GONE
                binding.flashboardSuggestionBar.visibility = android.view.View.VISIBLE
            } else {
                binding.flashboardUtilityBar.visibility = android.view.View.VISIBLE
                binding.flashboardSuggestionBar.visibility = android.view.View.GONE
            }
        }
    }

    companion object {
        private var animator: android.animation.ValueAnimator? = null
        val rgbMatrix = android.graphics.Matrix()
        var rgbOffset = 0f
        
        private val updateListeners = java.util.concurrent.CopyOnWriteArrayList<() -> Unit>()

        fun addRgbListener(listener: () -> Unit) {
            updateListeners.add(listener)
            startAnimatorIfNeeded()
        }

        fun removeRgbListener(listener: () -> Unit) {
            updateListeners.remove(listener)
            if (updateListeners.isEmpty()) {
                animator?.cancel()
                animator = null
            }
        }

        private fun startAnimatorIfNeeded() {
            if (animator == null) {
                animator = android.animation.ValueAnimator.ofFloat(0f, 1000f).apply {
                    duration = 3000
                    repeatCount = android.animation.ValueAnimator.INFINITE
                    interpolator = android.view.animation.LinearInterpolator()
                    addUpdateListener { anim ->
                        rgbOffset = anim.animatedValue as Float
                        rgbMatrix.setTranslate(rgbOffset, 0f)
                        updateListeners.forEach { it.invoke() }
                    }
                    start()
                }
            }
        }
    }
}
