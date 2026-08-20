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
import org.fossify.keyboard.helpers.Config
import org.fossify.keyboard.helpers.FlashboardThemeEngine
import org.fossify.keyboard.helpers.MyKeyboard

class FlashboardKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var code: Int = 0
    var type: String = "character"
    var label: String = ""
    var hint: String = ""

    var onKeyActionListener: ((code: Int, type: String, isLongPress: Boolean) -> Unit)? = null

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        style = Paint.Style.FILL
        clearShadowLayer()
    }

    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        style = Paint.Style.FILL
        clearShadowLayer()
    }

    private val textBounds = Rect()
    private val handler = Handler(Looper.getMainLooper())
    private var isLongPressed = false

    private val longPressRunnable = Runnable {
        if (isPressed) {
            isLongPressed = true
            performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            onKeyActionListener?.invoke(code, type, true)
        }
    }

    init {
        isClickable = true
        isFocusable = true
        
        context.theme.obtainStyledAttributes(attrs, R.styleable.FlashboardKeyView, 0, 0).apply {
            try {
                code = getInt(R.styleable.FlashboardKeyView_florisCode, 0)
                type = getString(R.styleable.FlashboardKeyView_florisType) ?: "character"
                label = getString(R.styleable.FlashboardKeyView_florisLabel) ?: ""
                hint = getString(R.styleable.FlashboardKeyView_florisHint) ?: ""
            } finally {
                recycle()
            }
        }
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applyThemeStyling()
    }

    fun applyThemeStyling() {
        val config = Config.newInstance(context)
        val isEnter = type == "enter" || code == MyKeyboard.KEYCODE_ENTER
        val isSpace = type == "space" || code == MyKeyboard.KEYCODE_SPACE
        val isModifier = type == "modifier" || type == "shift" || type == "delete"
        
        if (config.bgImageEnabled) {
            if (isSpace) {
                background = FlashboardThemeEngine.getSpacebarDrawable(context)
            } else if (isEnter) {
                background = FlashboardThemeEngine.getEnterKeyDrawable(context)
            } else if (type == "utility") {
                background = FlashboardThemeEngine.getUtilityChipDrawable(context)
            } else {
                // borderless fully transparent normal state, #26FFFFFF press state
                val pressed = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 22f * context.resources.displayMetrics.density
                    setColor(android.graphics.Color.parseColor("#26FFFFFF"))
                }
                val normal = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.TRANSPARENT)
                }
                background = android.graphics.drawable.StateListDrawable().apply {
                    addState(intArrayOf(android.R.attr.state_pressed), pressed)
                    addState(intArrayOf(), normal)
                }
            }
        } else {
            // fallback if needed
            val bgColor = android.graphics.Color.parseColor("#131316")
            val pressedColor = android.graphics.Color.parseColor("#26FFFFFF")
            val pressed = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 22f * context.resources.displayMetrics.density
                setColor(pressedColor)
            }
            val normal = android.graphics.drawable.GradientDrawable().apply {
                setColor(if(isSpace) android.graphics.Color.parseColor("#990006") else if(isEnter) android.graphics.Color.parseColor("#0066FF") else android.graphics.Color.TRANSPARENT)
            }
            background = android.graphics.drawable.StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_pressed), pressed)
                addState(intArrayOf(), normal)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        
        val isEnter = type == "enter" || code == MyKeyboard.KEYCODE_ENTER
        val isSpace = type == "space" || code == MyKeyboard.KEYCODE_SPACE
        
        textPaint.color = android.graphics.Color.WHITE
        hintPaint.color = android.graphics.Color.parseColor("#A0A4AC")
        
        // Draw icon if needed
        val iconDrawable = getIcon()
        if (iconDrawable != null) {
            val iconSize = (h * 0.45f).toInt()
            val left = ((w - iconSize) / 2).toInt()
            val top = ((h - iconSize) / 2).toInt()
            iconDrawable.setBounds(left, top, left + iconSize, top + iconSize)
            iconDrawable.setTint(android.graphics.Color.WHITE)
            iconDrawable.draw(canvas)
            return
        }

        // Draw label
        if (label.isNotEmpty()) {
            val config = Config.newInstance(context)
            val displayLabel = if (isSpace) config.customSpacebarText else label
            
            textPaint.textSize = if (displayLabel.length > 1) h * 0.35f else h * 0.45f
            if (isSpace || isEnter) textPaint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            
            textPaint.getTextBounds(displayLabel, 0, displayLabel.length, textBounds)
            val textX = w / 2f
            val textY = h / 2f - textBounds.exactCenterY()
            canvas.drawText(displayLabel, textX, textY, textPaint)
        }
        
        // Draw hint
        if (hint.isNotEmpty()) {
            hintPaint.textSize = h * 0.22f
            val padding = h * 0.15f
            val hintX = w - padding
            val hintY = padding + hintPaint.textSize * 0.8f
            canvas.drawText(hint, hintX, hintY, hintPaint)
        }
    }
    
    private fun getIcon(): Drawable? {
        return when {
            type == "shift" -> ContextCompat.getDrawable(context, R.drawable.ic_caps_outline)
            type == "delete" -> ContextCompat.getDrawable(context, R.drawable.ic_backspace_outline)
            type == "enter" -> ContextCompat.getDrawable(context, R.drawable.ic_return_outline)
            type == "globe" -> ContextCompat.getDrawable(context, R.drawable.ic_globe)
            code == MyKeyboard.KEYCODE_POPUP_EMOJI -> ContextCompat.getDrawable(context, R.drawable.ic_emoji_emotions_outline_vector)
            code == MyKeyboard.KEYCODE_POPUP_SETTINGS -> ContextCompat.getDrawable(context, R.drawable.ic_tune_vector)
            code == MyKeyboard.KEYCODE_POPUP_SETTINGS -> ContextCompat.getDrawable(context, R.drawable.ic_clipboard_vector)
            code == MyKeyboard.KEYCODE_POPUP_SETTINGS -> ContextCompat.getDrawable(context, R.drawable.ic_microphone_vector)
            else -> null
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                isLongPressed = false
                val config = Config.newInstance(context)
                if (config.vibrateOnKeypress) {
                    performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                }
                handler.postDelayed(longPressRunnable, 400)
            }
            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(longPressRunnable)
                if (isPressed && !isLongPressed) {
                    onKeyActionListener?.invoke(code, type, false)
                }
                isPressed = false
            }
            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                isPressed = false
            }
        }
        return super.onTouchEvent(event)
    }
}
