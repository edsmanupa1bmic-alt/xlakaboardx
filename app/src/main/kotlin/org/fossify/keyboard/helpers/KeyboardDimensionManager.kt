package org.fossify.keyboard.helpers

import android.content.Context
import android.util.TypedValue
import org.fossify.keyboard.extensions.config

/**
 * Dynamic Resizing & Font Scaling Engine.
 * Computes responsive row heights, smartbar dimensions, and key font sizes
 * based on user preferences (keyboard_height_scale and font_size_scale).
 */
object KeyboardDimensionManager {

    const val HEIGHT_VERY_SHORT = 85
    const val HEIGHT_SHORT = 92
    const val HEIGHT_NORMAL = 100
    const val HEIGHT_TALL = 108
    const val HEIGHT_EXTRA_TALL = 115

    const val FONT_SMALL = 80
    const val FONT_NORMAL = 100
    const val FONT_LARGE = 115
    const val FONT_EXTRA_LARGE = 130

    fun getKeyboardHeightScale(context: Context): Float {
        val percentage = context.config.keyboardHeightPercentage
        return (percentage / 100f).coerceIn(0.70f, 1.40f)
    }

    fun getFontScale(context: Context): Float {
        val percentage = context.config.fontScale
        return (percentage / 100f).coerceIn(0.70f, 1.40f)
    }

    fun getEmojiScale(context: Context): Float {
        val percentage = context.config.emojiScale
        return (percentage / 100f).coerceIn(0.70f, 1.50f)
    }

    fun getRowHeightPx(context: Context, baseRowHeightDp: Float = 44f): Int {
        val scale = getKeyboardHeightScale(context)
        return dpToPx(context, baseRowHeightDp * scale)
    }

    fun getSmartbarHeightPx(context: Context, baseHeightDp: Float = 44f): Int {
        val scale = getKeyboardHeightScale(context)
        return dpToPx(context, baseHeightDp * scale)
    }

    fun getRecentEmojiStripHeightPx(context: Context, baseHeightDp: Float = 36f): Int {
        val scale = getKeyboardHeightScale(context)
        return dpToPx(context, baseHeightDp * scale)
    }

    fun getScaledPrimaryTextSize(context: Context, keyHeight: Float, labelLength: Int): Float {
        val fontScale = getFontScale(context)
        val baseRatio = if (labelLength > 3) 0.28f else if (labelLength > 1) 0.34f else 0.42f
        return (keyHeight * baseRatio * fontScale).coerceIn(keyHeight * 0.18f, keyHeight * 0.60f)
    }

    fun getScaledSecondaryTextSize(context: Context, keyHeight: Float): Float {
        val fontScale = getFontScale(context)
        return (keyHeight * 0.23f * fontScale).coerceIn(keyHeight * 0.12f, keyHeight * 0.38f)
    }

    fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
    }
}
