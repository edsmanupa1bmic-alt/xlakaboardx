package org.fossify.keyboard.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.icu.text.BreakIterator
import android.icu.util.ULocale
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_DATETIME
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_MASK_CLASS
import android.text.InputType.TYPE_MASK_VARIATION
import android.text.InputType.TYPE_NULL
import android.text.TextUtils
import android.util.Size
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.fossify.keyboard.helpers.KeyboardDimensionManager

import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_ENTER_ACTION
import android.view.inputmethod.EditorInfo.IME_MASK_ACTION
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InlineSuggestionsRequest
import android.view.inputmethod.InlineSuggestionsResponse
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodSubtype
import android.widget.ImageView
import android.widget.TextView
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import org.fossify.commons.extensions.getSharedPrefs
import org.fossify.commons.helpers.isNougatPlus
import org.fossify.commons.helpers.isPiePlus
import org.fossify.keyboard.models.ParsedFlorisTheme
import org.fossify.keyboard.R
import org.fossify.keyboard.activities.SettingsActivity
import org.fossify.keyboard.extensions.*
import org.fossify.keyboard.helpers.*
import org.fossify.keyboard.interfaces.OnKeyboardActionListener
import org.fossify.keyboard.nlp.WordPredictionEngine
import org.fossify.keyboard.views.FlashboardKeyView

class SimpleKeyboardIME : InputMethodService(), OnKeyboardActionListener, SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private var SHIFT_PERM_TOGGLE_SPEED = 500
        const val KEYBOARD_LETTERS = 0
        const val KEYBOARD_SYMBOLS = 1
        const val KEYBOARD_SYMBOLS_SHIFT = 2
        const val KEYBOARD_NUMBERS = 3
        const val KEYBOARD_PHONE = 4
        const val KEYBOARD_SYMBOLS_ALT = 5
    }

    private var shiftState = ShiftState.OFF
    private var lastShiftPressTS = 0L
    private var keyboardMode = KEYBOARD_LETTERS
    private var inputTypeClass = TYPE_CLASS_TEXT
    private var inputTypeClassVariation = TYPE_CLASS_TEXT
    private var enterKeyType = IME_ACTION_NONE
    private var switchToLetters = false
    private var breakIterator: BreakIterator? = null
    private val sinhalaIME = SinhalaIME()

    private var flashboardRoot: ViewGroup? = null

    private val themeListener: (ParsedFlorisTheme) -> Unit = {
        setupThemeBackground(flashboardRoot)
    }

    private val layoutListener: () -> Unit = {}

    private val themeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            setupThemeBackground(flashboardRoot)
        }
    }

    override fun onCreate() {
        super.onCreate()
        EmojiManager.init(applicationContext)
        ThemeEngine.addListener(themeListener)
        CustomLayoutManager.addListener(layoutListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(themeChangeReceiver, IntentFilter("org.fossify.keyboard.THEME_CHANGED"), Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(themeChangeReceiver, IntentFilter("org.fossify.keyboard.THEME_CHANGED"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeEngine.removeListener(themeListener)
        CustomLayoutManager.removeListener(layoutListener)
        unregisterReceiver(themeChangeReceiver)
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        safeStorageContext.getSharedPrefs().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onEvaluateFullscreenMode(): Boolean = false
    override fun isExtractViewShown(): Boolean = false

    override fun onCreateInputView(): View {
        // 1. Force inflate our custom Flashboard XML
        val inflater = layoutInflater.cloneInContext(this)
        val inputView = inflater.inflate(R.layout.keyboard_root, null) as ViewGroup
        flashboardRoot = inputView

        WordPredictionEngine.getInstance().initPreferences(applicationContext)

        // 2. Initialize background image & dimming
        setupThemeBackground(inputView)

        // 3. Initialize Top Smartbar (Utility chips & Suggestion strip)
        setupSmartbar(inputView)

        // 4. Initialize Recent Emoji Ribbon
        setupRecentEmojis(inputView)

        // 5. Direct Key Binding (Attach Click / Touch Listeners directly to InputConnection)
        bindFlashboardKeys(inputView)

                // 6. Navigation Bar Insets Fix
        ViewCompat.setOnApplyWindowInsetsListener(inputView) { view, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val defaultPadding = KeyboardDimensionManager.dpToPx(this, 8f)
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                navInsets.bottom.coerceAtLeast(defaultPadding)
            )
            insets
        }
        
        return inputView
    }

    private fun setupThemeBackground(root: ViewGroup?) {
        if (root == null) return
        val bgImage = root.findViewById<ImageView>(R.id.keyboard_bg_image)
        val bgDim = root.findViewById<View>(R.id.keyboard_bg_dim)
        
        val config = Config.newInstance(this)
        val bitmap = FlashboardThemeEngine.getWallpaperBitmap(this)
        
        if (config.bgImageEnabled && bitmap != null) {
            bgImage?.setImageBitmap(bitmap)
            bgImage?.visibility = View.VISIBLE
            bgDim?.visibility = View.VISIBLE
            bgDim?.alpha = config.bgDimOpacity
            root.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            
            if (config.bgBlurRadius > 0f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bgImage?.setRenderEffect(
                    android.graphics.RenderEffect.createBlurEffect(
                        config.bgBlurRadius,
                        config.bgBlurRadius,
                        android.graphics.Shader.TileMode.CLAMP
                    )
                )
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    bgImage?.setRenderEffect(null)
                }
            }
        } else {
            bgImage?.visibility = View.GONE
            bgDim?.visibility = View.GONE
            root.setBackgroundColor(android.graphics.Color.parseColor("#131316"))
        }
    }

    private fun setupSmartbar(root: ViewGroup?) {
        if (root == null) return
        root.findViewById<View>(R.id.util_lang)?.setOnClickListener { onKey(MyKeyboard.KEYCODE_EMOJI_OR_LANGUAGE) }
        root.findViewById<View>(R.id.util_emoji)?.setOnClickListener { onKey(MyKeyboard.KEYCODE_POPUP_EMOJI) }
        root.findViewById<View>(R.id.util_settings)?.setOnClickListener { onKey(MyKeyboard.KEYCODE_POPUP_SETTINGS) }
        root.findViewById<View>(R.id.util_collapse)?.setOnClickListener { requestHideSelf(0) }
    }

    private fun setupRecentEmojis(root: ViewGroup?) {
        // Simple initialization
    }

    private fun bindFlashboardKeys(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is FlashboardKeyView) {
                child.onKeyActionListener = { code, type, isLongPress ->
                    if (isLongPress && child.hint.isNotEmpty()) {
                        val hintCode = child.hint[0].code
                        onKey(hintCode)
                    } else if (isLongPress && type == "delete") {
                        val inputConnection = currentInputConnection
                        inputConnection?.deleteSurroundingText(20, 0)
                    } else {
                        onKey(code)
                    }
                }
            } else if (child is ViewGroup) {
                bindFlashboardKeys(child)
            }
        }
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        setupThemeBackground(flashboardRoot)
    }

    override fun onPress(primaryCode: Int) {}

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        sinhalaIME.finishComposing(currentInputConnection)
        if (attribute != null) {
            inputTypeClass = attribute.inputType and TYPE_MASK_CLASS
            inputTypeClassVariation = attribute.inputType and TYPE_MASK_VARIATION
            enterKeyType = attribute.imeOptions and (IME_MASK_ACTION or IME_FLAG_NO_ENTER_ACTION)
        }
        
        if (isNougatPlus()) {
            breakIterator = BreakIterator.getCharacterInstance(ULocale.getDefault())
        }
        updateShiftKeyState()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        sinhalaIME.finishComposing(currentInputConnection)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        sinhalaIME.finishComposing(currentInputConnection)
    }

    private fun updateShiftKeyState() {
        if (shiftState == ShiftState.ON_PERMANENT) return

        val editorInfo = currentInputEditorInfo
        if (config.enableSentencesCapitalization && editorInfo != null && editorInfo.inputType != TYPE_NULL) {
            if (currentInputConnection?.getCursorCapsMode(editorInfo.inputType) != 0) {
                shiftState = ShiftState.ON_ONE_CHAR
                updateKeysShiftState()
                return
            }
        }

        shiftState = ShiftState.OFF
        updateKeysShiftState()
    }

    private fun updateKeysShiftState() {
        val root = flashboardRoot ?: return
        val isShifted = shiftState > ShiftState.OFF
        fun traverse(viewGroup: ViewGroup) {
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                if (child is FlashboardKeyView && child.label.isNotEmpty() && child.label.length == 1) {
                    child.label = if (isShifted) child.label.uppercase() else child.label.lowercase()
                    child.invalidate()
                } else if (child is ViewGroup) {
                    traverse(child)
                }
            }
        }
        traverse(root)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateInlineSuggestionsRequest(uiExtras: Bundle): InlineSuggestionsRequest {
        val maxWidth = resources.getDimensionPixelSize(R.dimen.suggestion_max_width)
        return InlineSuggestionsRequest.Builder(
            listOf(
                InlinePresentationSpec.Builder(
                    Size(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
                    Size(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
                ).build()
            )
        ).setMaxSuggestionCount(InlineSuggestionsRequest.SUGGESTION_COUNT_UNLIMITED).build()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onInlineSuggestionsResponse(response: InlineSuggestionsResponse): Boolean {
        return true
    }

    override fun onKey(code: Int) {
        val inputConnection = currentInputConnection
        if (inputConnection == null) return

        if (code != MyKeyboard.KEYCODE_SHIFT) {
            lastShiftPressTS = 0
        }

        when (code) {
            MyKeyboard.KEYCODE_DELETE -> {
                if (baseContext.config.keyboardLanguage == LANGUAGE_SINHALA_SINGLISH && sinhalaIME.isComposing) {
                    if (sinhalaIME.onBackspace(inputConnection)) return
                }
                val selectedText = inputConnection.getSelectedText(0)
                if (TextUtils.isEmpty(selectedText)) {
                    val count = getCountToDelete(inputConnection)
                    inputConnection.deleteSurroundingText(count, 0)
                } else {
                    inputConnection.commitText("", 1)
                }
            }

            MyKeyboard.KEYCODE_SHIFT -> {
                when {
                    shiftState == ShiftState.ON_PERMANENT -> shiftState = ShiftState.OFF
                    System.currentTimeMillis() - lastShiftPressTS < SHIFT_PERM_TOGGLE_SPEED -> shiftState = ShiftState.ON_PERMANENT
                    shiftState == ShiftState.ON_ONE_CHAR -> shiftState = ShiftState.OFF
                    shiftState == ShiftState.OFF -> shiftState = ShiftState.ON_ONE_CHAR
                }
                lastShiftPressTS = System.currentTimeMillis()
                updateKeysShiftState()
            }

            MyKeyboard.KEYCODE_ENTER -> {
                sinhalaIME.finishComposing(inputConnection)
                val imeOptionsActionId = getImeOptionsActionId()
                if (imeOptionsActionId != IME_ACTION_NONE) {
                    inputConnection.performEditorAction(imeOptionsActionId)
                } else {
                    inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }
            }

            MyKeyboard.KEYCODE_EMOJI_OR_LANGUAGE -> {
                sinhalaIME.finishComposing(inputConnection)
                val currentLang = config.keyboardLanguage
                if (currentLang == LANGUAGE_SINHALA_SINGLISH || currentLang == LANGUAGE_SINHALA_WIJESEKARA) {
                    config.keyboardLanguage = LANGUAGE_ENGLISH_QWERTY
                } else {
                    config.keyboardLanguage = LANGUAGE_SINHALA_SINGLISH
                }
            }

            MyKeyboard.KEYCODE_POPUP_EMOJI -> {
                sinhalaIME.finishComposing(inputConnection)
            }
            MyKeyboard.KEYCODE_POPUP_SETTINGS -> {
                sinhalaIME.finishComposing(inputConnection)
                Intent(this, SettingsActivity::class.java)
                    .apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
            }

            else -> {
                var codeChar = code.toChar()
                val originalText = inputConnection.getExtractedText(ExtractedTextRequest(), 0)?.text

                if (Character.isLetter(codeChar) && shiftState > ShiftState.OFF) {
                    codeChar = Character.toUpperCase(codeChar)
                }

                if (keyboardMode != KEYBOARD_LETTERS && inputTypeClass == TYPE_CLASS_TEXT && code == MyKeyboard.KEYCODE_SPACE) {
                    sinhalaIME.finishComposing(inputConnection)
                    inputConnection.commitText(codeChar.toString(), 1)
                    val newText = inputConnection.getExtractedText(ExtractedTextRequest(), 0)?.text
                    if (originalText != newText) {
                        switchToLetters = keyboardMode != KEYBOARD_SYMBOLS_ALT
                    }
                } else {
                    when {
                        baseContext.config.keyboardLanguage == LANGUAGE_SINHALA_SINGLISH -> {
                            if (code == MyKeyboard.KEYCODE_SPACE) {
                                sinhalaIME.finishComposing(inputConnection, autoCorrect = baseContext.config.smartAutoCorrection)
                                inputConnection.commitText(" ", 1)
                                updateShiftKeyState()
                            } else if (Character.isLetter(codeChar) || codeChar == '~' || codeChar == 'X' || codeChar == 'x' || codeChar == 'H') {
                                sinhalaIME.onInputCharacter(inputConnection, codeChar)
                                updateShiftKeyState()
                            } else {
                                sinhalaIME.finishComposing(inputConnection, autoCorrect = baseContext.config.smartAutoCorrection)
                                inputConnection.commitText(codeChar.toString(), 1)
                                updateShiftKeyState()
                            }
                        }
                        baseContext.config.keyboardLanguage == LANGUAGE_SINHALA_WIJESEKARA -> {
                            val currentWord = originalText?.toString()?.substringAfterLast(" ") ?: ""
                            val result = WijesekaraEngine.processKey(currentWord, codeChar)
                            if (result != null) {
                                val deleteCount = result.first
                                val replacement = result.second
                                if (deleteCount > 0) {
                                    inputConnection.deleteSurroundingText(deleteCount, 0)
                                }
                                inputConnection.commitText(replacement, 1)
                                updateShiftKeyState()
                            } else {
                                inputConnection.commitText(codeChar.toString(), 1)
                                updateShiftKeyState()
                            }
                        }
                        else -> {
                            if (code == MyKeyboard.KEYCODE_SPACE) {
                                applyEnglishAutoCorrectionIfNeeded(inputConnection)
                                sinhalaIME.finishComposing(inputConnection)
                                inputConnection.commitText(" ", 1)
                                updateShiftKeyState()
                            } else {
                                if (!Character.isLetterOrDigit(codeChar) && codeChar != '\'') {
                                    applyEnglishAutoCorrectionIfNeeded(inputConnection)
                                    sinhalaIME.finishComposing(inputConnection)
                                } else {
                                    sinhalaIME.onInputEnglishCharacter(inputConnection, codeChar)
                                }
                                inputConnection.commitText(codeChar.toString(), 1)
                                updateShiftKeyState()
                            }
                        }
                    }
                    
                    if (codeChar == '=') {
                        val newText = inputConnection.getExtractedText(ExtractedTextRequest(), 0)?.text?.toString() ?: ""
                        val currentWord = newText.substringAfterLast(" ")
                        val calcResult = InlineCalculator.calculate(currentWord)
                        if (calcResult != null) {
                            inputConnection.commitText(calcResult, 1)
                        }
                    }
                }
            }
        }
    }

    private fun applyEnglishAutoCorrectionIfNeeded(inputConnection: InputConnection) {
        if (!baseContext.config.smartAutoCorrection) return
        val textBefore = inputConnection.getTextBeforeCursor(40, 0)?.toString() ?: return
        val match = Regex("""([a-zA-Z'’]+)$""").find(textBefore)
        if (match != null) {
            val lastWord = match.groupValues[1]
            val corrected = WordPredictionEngine.getInstance().getAutoCorrection(lastWord, isSinglish = false)
            if (corrected != null && !corrected.equals(lastWord, ignoreCase = false)) {
                inputConnection.deleteSurroundingText(lastWord.length, 0)
                inputConnection.commitText(corrected, 1)
                WordPredictionEngine.getInstance().learnWord(corrected, false)
            }
        }
    }

    private fun getCountToDelete(inputConnection: InputConnection): Int {
        if (breakIterator == null || !isNougatPlus()) return 1
        val prevText = inputConnection.getTextBeforeCursor(8, 0)
        if (!TextUtils.isEmpty(prevText)) {
            return breakIterator?.let {
                it.setText(prevText.toString())
                val end = it.last()
                val start = it.previous()
                (end - (if (start == BreakIterator.DONE) 0 else start)).coerceIn(0, prevText?.length)
            } ?: 1
        }
        return 1
    }

    override fun onActionUp() {
        if (switchToLetters) {
            keyboardMode = KEYBOARD_LETTERS
            switchToLetters = false
        }
    }

    override fun moveCursorLeft() { moveCursor(false) }
    override fun moveCursorRight() { moveCursor(true) }
    override fun onText(text: String) { currentInputConnection?.commitText(text, 1) }

    override fun reloadKeyboard() { setupThemeBackground(flashboardRoot) }

    override fun changeInputMethod(id: String, subtype: InputMethodSubtype) {
        if (isPiePlus()) { switchInputMethod(id, subtype) } else { switchInputMethod(id) }
    }

    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        updateShiftKeyState()
    }

    override fun onUpdateCursorAnchorInfo(cursorAnchorInfo: CursorAnchorInfo?) {
        super.onUpdateCursorAnchorInfo(cursorAnchorInfo)
        updateShiftKeyState()
    }

    private fun moveCursor(moveRight: Boolean) {
        val inputConnection = currentInputConnection
        val extractedText = inputConnection?.getExtractedText(ExtractedTextRequest(), 0) ?: return
        val text = extractedText.text ?: return
        val oldPos = extractedText.selectionStart
        val newPos = if (moveRight) oldPos + 1 else oldPos - 1
        val clampedPos = newPos.coerceIn(0, text.length)

        if (clampedPos != oldPos) {
            inputConnection.setSelection(clampedPos, clampedPos)
        }
    }

    private fun getImeOptionsActionId(): Int {
        return if (currentInputEditorInfo.imeOptions and IME_FLAG_NO_ENTER_ACTION != 0) {
            IME_ACTION_NONE
        } else {
            currentInputEditorInfo.imeOptions and IME_MASK_ACTION
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sinhalaIME.finishComposing(currentInputConnection)
        setupThemeBackground(flashboardRoot)
    }
}
