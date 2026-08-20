package org.fossify.keyboard.views

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import org.fossify.keyboard.helpers.SinhalaIME

class RgbAnimatedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var isRgbMode = false
    private var linearGradient: LinearGradient? = null
    
    init {
        paint.style = android.graphics.Paint.Style.FILL
        setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
    }
    
    private val updateListener = {
        linearGradient?.setLocalMatrix(SinhalaIME.rgbMatrix)
        invalidate()
    }

    fun setRgbMode(enabled: Boolean) {
        if (isRgbMode == enabled) return
        isRgbMode = enabled
        if (enabled) {
            setupGradient()
            paint.shader = linearGradient
            SinhalaIME.addRgbListener(updateListener)
        } else {
            paint.shader = null
            SinhalaIME.removeRgbListener(updateListener)
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isRgbMode) {
            setupGradient()
            paint.shader = linearGradient
        }
    }

    private fun setupGradient() {
        if (width == 0) return
        val colors = intArrayOf(
            Color.RED, Color.YELLOW, Color.GREEN, 
            Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED
        )
        linearGradient = LinearGradient(
            0f, 0f, width.toFloat() * 2, 0f,
            colors, null, Shader.TileMode.REPEAT
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SinhalaIME.removeRgbListener(updateListener)
    }
}
