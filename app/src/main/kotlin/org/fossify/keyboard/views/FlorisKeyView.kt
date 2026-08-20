package org.fossify.keyboard.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import org.fossify.keyboard.R
import org.fossify.keyboard.helpers.FlorisStyleEvaluator
import org.fossify.keyboard.helpers.KeyboardDimensionManager
import org.fossify.keyboard.helpers.MyKeyboard
import org.fossify.keyboard.models.CustomKeyDef
import org.fossify.keyboard.models.ParsedFlorisTheme

/**
 * Custom View representing a single FlorisBoard key with dynamic CSS shape rendering,
 * centered main label / icon, top-right secondary hint, and touch interaction with long-press support.
 */
class FlorisKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var keyDef: CustomKeyDef = CustomKeyDef()
        private set

    var theme: ParsedFlorisTheme = ParsedFlorisTheme(id = "default", name = "Default")
        private set

    var isShifted: Boolean = false
        private set

    var isCapsLock: Boolean = false
        private set

    var onKeyActionListener: ((keyDef: CustomKeyDef, isLongPress: Boolean) -> Unit)? = null

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        style = Paint.Style.FILL
        clearShadowLayer()
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        style = Paint.Style.FILL
        clearShadowLayer()
    }

    private var isRgbMode = false
    private var linearGradient: android.graphics.LinearGradient? = null
    
    private val rgbUpdateListener = {
        linearGradient?.setLocalMatrix(org.fossify.keyboard.helpers.SinhalaIME.rgbMatrix)
        invalidate()
    }

    private val textBounds = Rect()
    private val handler = Handler(Looper.getMainLooper())
    private var isLongPressed = false
    private var isRepeating = false

    private val longPressRunnable = Runnable {
        if (isPressed) {
            isLongPressed = true
            performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            onKeyActionListener?.invoke(keyDef, true)
            if (keyDef.code == MyKeyboard.KEYCODE_DELETE) {
                startRepeatBackspace()
            }
        }
    }

    private val repeatRunnable = object : Runnable {
        override fun run() {
            if (isPressed && isLongPressed && keyDef.code == MyKeyboard.KEYCODE_DELETE) {
                onKeyActionListener?.invoke(keyDef, false)
                handler.postDelayed(this, 50)
            }
        }
    }

    init {
        isClickable = true
        isFocusable = true
    }

    fun setKeyData(
        def: CustomKeyDef,
        activeTheme: ParsedFlorisTheme,
        shifted: Boolean = false,
        caps: Boolean = false
    ) {
        this.keyDef = def
        this.theme = activeTheme
        this.isShifted = shifted
        this.isCapsLock = caps

        applyThemeStyling()
        invalidate()
    }

    fun setShiftState(shifted: Boolean, caps: Boolean) {
        this.isShifted = shifted
        this.isCapsLock = caps
        invalidate()
    }

    private fun setupGradientIfNeeded() {
        val w = width.toFloat()
        if (w == 0f) return
        if (linearGradient == null) {
            val colors = intArrayOf(
                android.graphics.Color.RED, android.graphics.Color.YELLOW, android.graphics.Color.GREEN, 
                android.graphics.Color.CYAN, android.graphics.Color.BLUE, android.graphics.Color.MAGENTA, android.graphics.Color.RED
            )
            linearGradient = android.graphics.LinearGradient(
                0f, 0f, w * 2, 0f,
                colors, null, android.graphics.Shader.TileMode.REPEAT
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        linearGradient = null
        if (isRgbMode) {
            setupGradientIfNeeded()
            textPaint.shader = linearGradient
            subTextPaint.shader = linearGradient
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isRgbMode) {
            org.fossify.keyboard.helpers.SinhalaIME.addRgbListener(rgbUpdateListener)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        org.fossify.keyboard.helpers.SinhalaIME.removeRgbListener(rgbUpdateListener)
    }

    private fun applyThemeStyling() {
        val config = org.fossify.keyboard.helpers.Config.newInstance(context)
        val newRgbMode = config.rgbTextEnabled
        if (isRgbMode != newRgbMode) {
            isRgbMode = newRgbMode
            if (isRgbMode) {
                setupGradientIfNeeded()
                textPaint.shader = linearGradient
                subTextPaint.shader = linearGradient
                org.fossify.keyboard.helpers.SinhalaIME.addRgbListener(rgbUpdateListener)
            } else {
                textPaint.shader = null
                subTextPaint.shader = null
                org.fossify.keyboard.helpers.SinhalaIME.removeRgbListener(rgbUpdateListener)
            }
        }

        val isAction = keyDef.code == MyKeyboard.KEYCODE_ENTER || keyDef.type == "enter" || keyDef.type == "accent"
        val computedStyle = FlorisStyleEvaluator.evaluateKeyStyle(context, theme, keyDef.type, keyDef.code, isAction)
        background = FlorisStyleEvaluator.createKeyDrawable(context, computedStyle)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val isAction = keyDef.code == MyKeyboard.KEYCODE_ENTER || keyDef.type == "enter" || keyDef.type == "accent"
        val isSpace = keyDef.code == MyKeyboard.KEYCODE_SPACE || keyDef.type == "space"
        val computedStyle = FlorisStyleEvaluator.evaluateKeyStyle(context, theme, keyDef.type, keyDef.code, isAction)
        val fgColor = computedStyle.foregroundColor

        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Draw Icon if applicable
        val iconDrawable = getIconForCode(keyDef.code, keyDef.type)
        if (iconDrawable != null) {
            val iconSize = (h * 0.44f).toInt()
            val left = ((w - iconSize) / 2).toInt()
            val top = ((h - iconSize) / 2).toInt()
            iconDrawable.setBounds(left, top, left + iconSize, top + iconSize)
            iconDrawable.setTint(fgColor)
            iconDrawable.draw(canvas)
            return
        }

        // 2. Draw Main Label
        val displayLabel = getEffectiveLabel()
        if (displayLabel.isNotEmpty()) {
            textPaint.color = fgColor
            textPaint.textSize = KeyboardDimensionManager.getScaledPrimaryTextSize(context, h, displayLabel.length)
            textPaint.typeface = if (isAction || isSpace) {
                Typeface.create("sans-serif", Typeface.NORMAL)
            } else if (displayLabel.length > 1) {
                Typeface.create("sans-serif-light", Typeface.NORMAL)
            } else {
                Typeface.create("sans-serif-light", Typeface.NORMAL)
            }

            textPaint.getTextBounds(displayLabel, 0, displayLabel.length, textBounds)
            val textY = (h / 2f) + (textBounds.height() / 2f) - textBounds.bottom
            canvas.drawText(displayLabel, w / 2f, textY, textPaint)
        }

        // 3. Draw Top-Right Secondary Hint (Digits or Popup symbol) in subtle muted gray
        val subLabel = keyDef.topSmallNumber.ifEmpty {
            keyDef.popupCharacters.firstOrNull().orEmpty()
        }

        if (subLabel.isNotEmpty() && displayLabel.length <= 1) {
            subTextPaint.color = computedStyle.secondaryTextColor
            subTextPaint.textSize = KeyboardDimensionManager.getScaledSecondaryTextSize(context, h)
            subTextPaint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)

            val rightPadding = w * 0.12f
            val topPadding = h * 0.28f
            canvas.drawText(subLabel, w - rightPadding, topPadding, subTextPaint)
        }
    }

    private fun getEffectiveLabel(): String {
        var lbl = keyDef.label
        if (keyDef.type == "space" || keyDef.code == MyKeyboard.KEYCODE_SPACE) {
            val config = org.fossify.keyboard.helpers.Config.newInstance(context)
            return if (lbl.isNotEmpty()) lbl else config.customSpacebarText
        }
        if (lbl.length == 1) {
            val ch = lbl[0]
            if (Character.isLetter(ch)) {
                lbl = if (isShifted || isCapsLock) ch.uppercase() else ch.lowercase()
            }
        }
        return lbl
    }

    private fun getIconForCode(code: Int, type: String): Drawable? {
        return when {
            code == MyKeyboard.KEYCODE_DELETE || type == "delete" ->
                ContextCompat.getDrawable(context, R.drawable.ic_delete_vector)
            code == MyKeyboard.KEYCODE_SHIFT || type == "shift" -> {
                when {
                    isCapsLock -> ContextCompat.getDrawable(context, R.drawable.ic_caps_underlined_vector)
                    isShifted -> ContextCompat.getDrawable(context, R.drawable.ic_caps_vector)
                    else -> ContextCompat.getDrawable(context, R.drawable.ic_caps_outline_vector)
                }
            }
            code == MyKeyboard.KEYCODE_ENTER || type == "enter" ->
                ContextCompat.getDrawable(context, R.drawable.ic_keyboard_return_vector)
                    ?: ContextCompat.getDrawable(context, R.drawable.ic_check_vector)
            code == MyKeyboard.KEYCODE_MODE_CHANGE || type == "mode_change" ->
                ContextCompat.getDrawable(context, R.drawable.ic_globe_vector)
            code == MyKeyboard.KEYCODE_POPUP_SETTINGS ->
                ContextCompat.getDrawable(context, org.fossify.commons.R.drawable.ic_settings_cog_vector)
            code == MyKeyboard.KEYCODE_POPUP_EMOJI || type == "emoji" ->
                ContextCompat.getDrawable(context, R.drawable.ic_emoji_emotions_outline_vector)
            else -> null
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                isLongPressed = false
                handler.postDelayed(longPressRunnable, 350)
                performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!pointInView(event.x, event.y)) {
                    isPressed = false
                    handler.removeCallbacks(longPressRunnable)
                    stopRepeatBackspace()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isPressed) {
                    handler.removeCallbacks(longPressRunnable)
                    stopRepeatBackspace()
                    if (!isLongPressed) {
                        onKeyActionListener?.invoke(keyDef, false)
                    }
                    isPressed = false
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                handler.removeCallbacks(longPressRunnable)
                stopRepeatBackspace()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun pointInView(x: Float, y: Float): Boolean {
        return x >= 0 && x <= width && y >= 0 && y <= height
    }

    private fun startRepeatBackspace() {
        if (!isRepeating) {
            isRepeating = true
            handler.postDelayed(repeatRunnable, 50)
        }
    }

    private fun stopRepeatBackspace() {
        isRepeating = false
        handler.removeCallbacks(repeatRunnable)
    }
}
