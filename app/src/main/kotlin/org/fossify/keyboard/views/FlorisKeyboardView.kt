package org.fossify.keyboard.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import org.fossify.keyboard.helpers.CustomLayoutManager
import org.fossify.keyboard.helpers.FlorisStyleEvaluator
import org.fossify.keyboard.helpers.KeyboardDimensionManager
import org.fossify.keyboard.helpers.MyKeyboard
import org.fossify.keyboard.helpers.ThemeEngine
import org.fossify.keyboard.models.CustomKeyboardLayout
import org.fossify.keyboard.models.CustomKeyDef
import org.fossify.keyboard.models.CustomRowDef
import org.fossify.keyboard.models.ParsedFlorisTheme

/**
 * Dynamic Key Matrix Renderer mirroring FlorisBoard's flexbox view architecture.
 * Programmatically constructs custom LinearLayout rows and FlorisKeyView items
 * using layout weights, cascading theme stylesheets, shift/caps states, and symbols mode.
 */
class FlorisKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    enum class ShiftState {
        OFF,
        ON_ONE_CHAR,
        ON_PERMANENT
    }

    var currentShiftState: ShiftState = ShiftState.OFF
        private set

    var isSymbolsMode: Boolean = false
        private set

    private var activeLayout: CustomKeyboardLayout? = null
    private var activeTheme: ParsedFlorisTheme = ThemeEngine.getActiveTheme(context)
    private val keyViews = ArrayList<FlorisKeyView>()
    private var lastShiftPressTime = 0L

    var onKeyEventListener: ((code: Int, label: String, isLongPress: Boolean) -> Unit)? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    /**
     * Initializes or reloads the keyboard with custom or built-in layout and active theme.
     */
    fun setupKeyboard(
        layout: CustomKeyboardLayout? = null,
        theme: ParsedFlorisTheme = ThemeEngine.getActiveTheme(context)
    ) {
        this.activeTheme = theme
        this.activeLayout = layout ?: CustomLayoutManager.getActiveLayout(context) ?: getDefaultAlphaLayout()
        this.isSymbolsMode = false
        this.currentShiftState = ShiftState.OFF

        applyBackground()
        renderKeyMatrix()
    }

    fun applyTheme(theme: ParsedFlorisTheme) {
        this.activeTheme = theme
        applyBackground()
        val isShifted = currentShiftState != ShiftState.OFF
        val isCaps = currentShiftState == ShiftState.ON_PERMANENT
        for (kv in keyViews) {
            kv.setKeyData(kv.keyDef, activeTheme, isShifted, isCaps)
        }
    }

    fun updateDimensions() {
        renderKeyMatrix()
    }

    private fun applyBackground() {
        background = FlorisStyleEvaluator.createKeyboardBackground(context, activeTheme)
    }

    private fun renderKeyMatrix() {
        removeAllViews()
        keyViews.clear()

        val layoutToRender = if (isSymbolsMode) getSymbolsLayout() else (activeLayout ?: getDefaultAlphaLayout())
        val isShifted = currentShiftState != ShiftState.OFF
        val isCaps = currentShiftState == ShiftState.ON_PERMANENT

        val rowHeightPx = KeyboardDimensionManager.getRowHeightPx(context, 44f)
        val keyMarginHorizontal = KeyboardDimensionManager.dpToPx(context, 2f)
        val keyMarginVertical = KeyboardDimensionManager.dpToPx(context, 2.5f)

        for (row in layoutToRender.rows) {
            val rowLayout = LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, rowHeightPx).apply {
                    setMargins(0, keyMarginVertical, 0, keyMarginVertical)
                }
                weightSum = row.keys.sumOf { it.widthWeight.toDouble() }.toFloat()
            }

            for (keyDef in row.keys) {
                val keyView = FlorisKeyView(context).apply {
                    layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, keyDef.widthWeight).apply {
                        setMargins(keyMarginHorizontal, 0, keyMarginHorizontal, 0)
                    }
                    setKeyData(keyDef, activeTheme, isShifted, isCaps)
                    onKeyActionListener = { def, isLongPress ->
                        handleKeyAction(def, isLongPress)
                    }
                }
                keyViews.add(keyView)
                rowLayout.addView(keyView)
            }

            addView(rowLayout)
        }
    }

    private fun handleKeyAction(keyDef: CustomKeyDef, isLongPress: Boolean) {
        when (keyDef.code) {
            MyKeyboard.KEYCODE_SHIFT -> {
                val now = System.currentTimeMillis()
                currentShiftState = when {
                    now - lastShiftPressTime < 350 -> ShiftState.ON_PERMANENT
                    currentShiftState == ShiftState.OFF -> ShiftState.ON_ONE_CHAR
                    currentShiftState == ShiftState.ON_ONE_CHAR -> ShiftState.OFF
                    else -> ShiftState.OFF
                }
                lastShiftPressTime = now
                updateKeyShiftStates()
                onKeyEventListener?.invoke(keyDef.code, keyDef.label, false)
            }
            MyKeyboard.KEYCODE_SYMBOLS_MODE_CHANGE -> {
                isSymbolsMode = !isSymbolsMode
                renderKeyMatrix()
                onKeyEventListener?.invoke(keyDef.code, keyDef.label, false)
            }
            MyKeyboard.KEYCODE_MODE_CHANGE -> {
                isSymbolsMode = false
                renderKeyMatrix()
                onKeyEventListener?.invoke(keyDef.code, keyDef.label, false)
            }
            else -> {
                var code = keyDef.code
                var label = keyDef.label

                if (isLongPress && keyDef.topSmallNumber.isNotEmpty()) {
                    label = keyDef.topSmallNumber
                    code = label.firstOrNull()?.code ?: code
                } else if (label.length == 1 && Character.isLetter(label[0])) {
                    val ch = if (currentShiftState != ShiftState.OFF) label[0].uppercaseChar() else label[0].lowercaseChar()
                    label = ch.toString()
                    code = ch.code

                    if (currentShiftState == ShiftState.ON_ONE_CHAR) {
                        currentShiftState = ShiftState.OFF
                        updateKeyShiftStates()
                    }
                }

                onKeyEventListener?.invoke(code, label, isLongPress)
            }
        }
    }

    private fun updateKeyShiftStates() {
        val isShifted = currentShiftState != ShiftState.OFF
        val isCaps = currentShiftState == ShiftState.ON_PERMANENT
        for (kv in keyViews) {
            kv.setShiftState(isShifted, isCaps)
        }
    }

    /**
     * Default Layout matching the required 5-layer visual hierarchy:
     * Layer 2: Dedicated Number Row [1..0]
     * Layer 3: QWERTY Row [q..p] with top-right secondary numbers (1..0)
     * Layer 4: Home Row [a..l] with secondary symbol sub-labels (@, #, $, -, &, _, +, (, ))
     * Layer 5: Bottom Row [Shift, z..m with secondary symbols (*, ", ', :, ;, !, ?), Backspace]
     * Layer 6: Action Row [123, Globe, Comma, Red Spacebar "xLakaBoardx", Dot, Blue Enter]
     */
    private fun getDefaultAlphaLayout(): CustomKeyboardLayout {
        // Layer 2: Dedicated Standalone Number Row
        val numberRow = CustomRowDef(keys = listOf(
            CustomKeyDef(code = '1'.code, label = "1", widthWeight = 1.0f),
            CustomKeyDef(code = '2'.code, label = "2", widthWeight = 1.0f),
            CustomKeyDef(code = '3'.code, label = "3", widthWeight = 1.0f),
            CustomKeyDef(code = '4'.code, label = "4", widthWeight = 1.0f),
            CustomKeyDef(code = '5'.code, label = "5", widthWeight = 1.0f),
            CustomKeyDef(code = '6'.code, label = "6", widthWeight = 1.0f),
            CustomKeyDef(code = '7'.code, label = "7", widthWeight = 1.0f),
            CustomKeyDef(code = '8'.code, label = "8", widthWeight = 1.0f),
            CustomKeyDef(code = '9'.code, label = "9", widthWeight = 1.0f),
            CustomKeyDef(code = '0'.code, label = "0", widthWeight = 1.0f)
        ), isNumbersRow = true)

        // Layer 3: QWERTY Row with secondary numbers (1-0)
        val qwertyRow = CustomRowDef(keys = listOf(
            CustomKeyDef(code = 'q'.code, label = "q", topSmallNumber = "1", widthWeight = 1.0f),
            CustomKeyDef(code = 'w'.code, label = "w", topSmallNumber = "2", widthWeight = 1.0f),
            CustomKeyDef(code = 'e'.code, label = "e", topSmallNumber = "3", widthWeight = 1.0f),
            CustomKeyDef(code = 'r'.code, label = "r", topSmallNumber = "4", widthWeight = 1.0f),
            CustomKeyDef(code = 't'.code, label = "t", topSmallNumber = "5", widthWeight = 1.0f),
            CustomKeyDef(code = 'y'.code, label = "y", topSmallNumber = "6", widthWeight = 1.0f),
            CustomKeyDef(code = 'u'.code, label = "u", topSmallNumber = "7", widthWeight = 1.0f),
            CustomKeyDef(code = 'i'.code, label = "i", topSmallNumber = "8", widthWeight = 1.0f),
            CustomKeyDef(code = 'o'.code, label = "o", topSmallNumber = "9", widthWeight = 1.0f),
            CustomKeyDef(code = 'p'.code, label = "p", topSmallNumber = "0", widthWeight = 1.0f)
        ))

        // Layer 4: Home Row with secondary symbols (@, #, $, -, &, _, +, (, ))
        val homeRow = CustomRowDef(keys = listOf(
            CustomKeyDef(code = 'a'.code, label = "a", topSmallNumber = "@", widthWeight = 1.0f),
            CustomKeyDef(code = 's'.code, label = "s", topSmallNumber = "#", widthWeight = 1.0f),
            CustomKeyDef(code = 'd'.code, label = "d", topSmallNumber = "$", widthWeight = 1.0f),
            CustomKeyDef(code = 'f'.code, label = "f", topSmallNumber = "-", widthWeight = 1.0f),
            CustomKeyDef(code = 'g'.code, label = "g", topSmallNumber = "&", widthWeight = 1.0f),
            CustomKeyDef(code = 'h'.code, label = "h", topSmallNumber = "_", widthWeight = 1.0f),
            CustomKeyDef(code = 'j'.code, label = "j", topSmallNumber = "+", widthWeight = 1.0f),
            CustomKeyDef(code = 'k'.code, label = "k", topSmallNumber = "(", widthWeight = 1.0f),
            CustomKeyDef(code = 'l'.code, label = "l", topSmallNumber = ")", widthWeight = 1.0f)
        ))

        // Layer 5: Bottom Row [Shift, z..m with secondary symbols (*, ", ', :, ;, !, ?), Backspace]
        val bottomRow = CustomRowDef(keys = listOf(
            CustomKeyDef(type = "shift", code = MyKeyboard.KEYCODE_SHIFT, label = "Shift", widthWeight = 1.4f),
            CustomKeyDef(code = 'z'.code, label = "z", topSmallNumber = "*", widthWeight = 1.0f),
            CustomKeyDef(code = 'x'.code, label = "x", topSmallNumber = "\"", widthWeight = 1.0f),
            CustomKeyDef(code = 'c'.code, label = "c", topSmallNumber = "'", widthWeight = 1.0f),
            CustomKeyDef(code = 'v'.code, label = "v", topSmallNumber = ":", widthWeight = 1.0f),
            CustomKeyDef(code = 'b'.code, label = "b", topSmallNumber = ";", widthWeight = 1.0f),
            CustomKeyDef(code = 'n'.code, label = "n", topSmallNumber = "!", widthWeight = 1.0f),
            CustomKeyDef(code = 'm'.code, label = "m", topSmallNumber = "?", widthWeight = 1.0f),
            CustomKeyDef(type = "delete", code = MyKeyboard.KEYCODE_DELETE, label = "Del", widthWeight = 1.4f)
        ))

        // Layer 6: Action Row [123, Globe, Comma, Red Spacebar "xLakaBoardx", Dot, Blue Enter]
        val actionRow = CustomRowDef(keys = listOf(
            CustomKeyDef(type = "symbols_mode_change", code = MyKeyboard.KEYCODE_SYMBOLS_MODE_CHANGE, label = "?123", widthWeight = 1.3f),
            CustomKeyDef(type = "mode_change", code = MyKeyboard.KEYCODE_MODE_CHANGE, label = "🌐", widthWeight = 1.0f),
            CustomKeyDef(type = "character", code = ','.code, label = ",", widthWeight = 0.9f),
            CustomKeyDef(type = "space", code = MyKeyboard.KEYCODE_SPACE, label = "xLakaBoardx", widthWeight = 4.2f),
            CustomKeyDef(type = "character", code = '.'.code, label = ".", widthWeight = 0.9f),
            CustomKeyDef(type = "enter", code = MyKeyboard.KEYCODE_ENTER, label = "Enter", widthWeight = 1.4f)
        ))

        return CustomKeyboardLayout(
            id = "default_flashboard_layout",
            name = "xLakaBoardx Layout",
            rows = listOf(numberRow, qwertyRow, homeRow, bottomRow, actionRow)
        )
    }

    private fun getSymbolsLayout(): CustomKeyboardLayout {
        val row1 = CustomRowDef(keys = listOf(
            CustomKeyDef(code = '1'.code, label = "1", widthWeight = 1.0f),
            CustomKeyDef(code = '2'.code, label = "2", widthWeight = 1.0f),
            CustomKeyDef(code = '3'.code, label = "3", widthWeight = 1.0f),
            CustomKeyDef(code = '4'.code, label = "4", widthWeight = 1.0f),
            CustomKeyDef(code = '5'.code, label = "5", widthWeight = 1.0f),
            CustomKeyDef(code = '6'.code, label = "6", widthWeight = 1.0f),
            CustomKeyDef(code = '7'.code, label = "7", widthWeight = 1.0f),
            CustomKeyDef(code = '8'.code, label = "8", widthWeight = 1.0f),
            CustomKeyDef(code = '9'.code, label = "9", widthWeight = 1.0f),
            CustomKeyDef(code = '0'.code, label = "0", widthWeight = 1.0f)
        ))

        val row2 = CustomRowDef(keys = listOf(
            CustomKeyDef(code = '@'.code, label = "@", widthWeight = 1.0f),
            CustomKeyDef(code = '#'.code, label = "#", widthWeight = 1.0f),
            CustomKeyDef(code = '$'.code, label = "$", widthWeight = 1.0f),
            CustomKeyDef(code = '_'.code, label = "_", widthWeight = 1.0f),
            CustomKeyDef(code = '&'.code, label = "&", widthWeight = 1.0f),
            CustomKeyDef(code = '-'.code, label = "-", widthWeight = 1.0f),
            CustomKeyDef(code = '+'.code, label = "+", widthWeight = 1.0f),
            CustomKeyDef(code = '('.code, label = "(", widthWeight = 1.0f),
            CustomKeyDef(code = ')'.code, label = ")", widthWeight = 1.0f),
            CustomKeyDef(code = '/'.code, label = "/", widthWeight = 1.0f)
        ))

        val row3 = CustomRowDef(keys = listOf(
            CustomKeyDef(code = '*'.code, label = "*", widthWeight = 1.0f),
            CustomKeyDef(code = '"'.code, label = "\"", widthWeight = 1.0f),
            CustomKeyDef(code = '\''.code, label = "'", widthWeight = 1.0f),
            CustomKeyDef(code = ':'.code, label = ":", widthWeight = 1.0f),
            CustomKeyDef(code = ';'.code, label = ";", widthWeight = 1.0f),
            CustomKeyDef(code = '!'.code, label = "!", widthWeight = 1.0f),
            CustomKeyDef(code = '?'.code, label = "?", widthWeight = 1.0f),
            CustomKeyDef(code = '%'.code, label = "%", widthWeight = 1.0f),
            CustomKeyDef(code = '='.code, label = "=", widthWeight = 1.0f),
            CustomKeyDef(code = '<'.code, label = "<", widthWeight = 1.0f)
        ))

        val row4 = CustomRowDef(keys = listOf(
            CustomKeyDef(type = "symbols_mode_change", code = MyKeyboard.KEYCODE_SYMBOLS_MODE_CHANGE, label = "ABC", widthWeight = 1.4f),
            CustomKeyDef(code = '>'.code, label = ">", widthWeight = 1.0f),
            CustomKeyDef(code = '['.code, label = "[", widthWeight = 1.0f),
            CustomKeyDef(code = ']'.code, label = "]", widthWeight = 1.0f),
            CustomKeyDef(code = '{'.code, label = "{", widthWeight = 1.0f),
            CustomKeyDef(code = '}'.code, label = "}", widthWeight = 1.0f),
            CustomKeyDef(code = '~'.code, label = "~", widthWeight = 1.0f),
            CustomKeyDef(code = '^'.code, label = "^", widthWeight = 1.0f),
            CustomKeyDef(type = "delete", code = MyKeyboard.KEYCODE_DELETE, label = "Del", widthWeight = 1.4f)
        ))

        val row5 = CustomRowDef(keys = listOf(
            CustomKeyDef(type = "symbols_mode_change", code = MyKeyboard.KEYCODE_SYMBOLS_MODE_CHANGE, label = "ABC", widthWeight = 1.3f),
            CustomKeyDef(type = "mode_change", code = MyKeyboard.KEYCODE_MODE_CHANGE, label = "🌐", widthWeight = 1.0f),
            CustomKeyDef(type = "character", code = ','.code, label = ",", widthWeight = 0.9f),
            CustomKeyDef(type = "space", code = MyKeyboard.KEYCODE_SPACE, label = "xLakaBoardx", widthWeight = 4.2f),
            CustomKeyDef(type = "character", code = '.'.code, label = ".", widthWeight = 0.9f),
            CustomKeyDef(type = "enter", code = MyKeyboard.KEYCODE_ENTER, label = "Enter", widthWeight = 1.4f)
        ))

        return CustomKeyboardLayout(
            id = "symbols_layout",
            name = "Symbols",
            rows = listOf(row1, row2, row3, row4, row5)
        )
    }
}
