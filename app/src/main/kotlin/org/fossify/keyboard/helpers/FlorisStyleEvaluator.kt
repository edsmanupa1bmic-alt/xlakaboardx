package org.fossify.keyboard.helpers

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import org.fossify.keyboard.R
import org.fossify.keyboard.models.ParsedFlorisTheme
import java.io.File

/**
 * FlorisBoard CSS Selector & Theme Cascading Evaluator.
 * Resolves cascading stylesheet rules for keyboard components:
 * `keyboard`, `key`, `key[type=character]`, `key[type=modifier]`, `key[type=accent]`,
 * `key[code=32]`, `key:pressed`, `smartbar`, `smartbar-chip`, and `popup`.
 */
object FlorisStyleEvaluator {

    data class FlorisComputedStyle(
        val backgroundColor: Int,
        val backgroundPressedColor: Int,
        val foregroundColor: Int,
        val secondaryTextColor: Int,
        val strokeColor: Int,
        val strokeWidth: Int,
        val cornerRadius: Float,
        val isAccent: Boolean = false,
        val isSpacebar: Boolean = false,
        val isEnter: Boolean = false,
        val isModifier: Boolean = false
    )

    /**
     * Resolves the computed styling for a key given the active Floris theme and key properties.
     */
    fun evaluateKeyStyle(
        context: Context,
        theme: ParsedFlorisTheme,
        keyType: String,
        keyCode: Int = 0,
        isAction: Boolean = false
    ): FlorisComputedStyle {
        val config = Config.newInstance(context)
        val isEnter = isAction || keyType == "enter" || keyCode == MyKeyboard.KEYCODE_ENTER
        val isSpace = keyType == "space" || keyCode == MyKeyboard.KEYCODE_SPACE
        val isModifier = keyType == "modifier" || keyType == "shift" || keyType == "delete" ||
                         keyType == "symbols_mode_change" || keyType == "mode_change" ||
                         keyType == "emoji"

        var bgColor: Int
        var bgPressedColor: Int
        var fgColor: Int
        val secondaryFgColor = if (theme.isNight) Color.parseColor("#8E929B") else Color.parseColor("#717680")
        val strokeColor = theme.strokeColor
        val strokeWidth = theme.strokeWidth
        val cornerRadius = if (isSpace || isEnter) 24f else (if (isModifier) 16f else theme.cornerRadius)

        when {
            isEnter -> {
                bgColor = Color.parseColor("#0066FF")
                bgPressedColor = Color.parseColor("#26FFFFFF")
                fgColor = config.keyTextColor
            }
            isSpace -> {
                bgColor = config.spacebarColor
                bgPressedColor = Color.parseColor("#26FFFFFF")
                fgColor = config.keyTextColor
            }
            isModifier -> {
                bgColor = if (theme.isNight) Color.parseColor("#22FFFFFF") else Color.parseColor("#D7DEEB")
                bgPressedColor = Color.parseColor("#26FFFFFF")
                fgColor = config.keyTextColor
            }
            else -> {
                // Modern Borderless & High Readability: Clean, borderless transparent / semi-flat touch targets
                bgColor = if (theme.isNight) Color.parseColor("#15FFFFFF") else Color.parseColor("#1EFFFFFF")
                bgPressedColor = Color.parseColor("#26FFFFFF")
                fgColor = config.keyTextColor
            }
        }
        
        if (config.bgImageEnabled) {
            val alpha = config.keyTranslucencyAlpha.coerceIn(0, 255)
            bgColor = Color.argb(alpha, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor))
        }

        return FlorisComputedStyle(
            backgroundColor = bgColor,
            backgroundPressedColor = bgPressedColor,
            foregroundColor = fgColor,
            secondaryTextColor = secondaryFgColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            isAccent = isEnter,
            isSpacebar = isSpace,
            isEnter = isEnter,
            isModifier = isModifier
        )
    }

    /**
     * Constructs a dynamic Ripple / Gradient Drawable for a key view according to its computed style.
     */
    fun createKeyDrawable(context: Context, style: FlorisComputedStyle): Drawable {
        val config = Config.newInstance(context)
        if (config.bgImageEnabled) {
            if (style.isSpacebar) return FlashboardThemeEngine.getSpacebarDrawable(context)
            if (style.isEnter) return FlashboardThemeEngine.getEnterKeyDrawable(context)
            return FlashboardThemeEngine.getTranslucentKeyDrawable(context, config.keyTranslucencyAlpha)
        }

        if (style.isSpacebar) {
            return ContextCompat.getDrawable(context, R.drawable.bg_spacebar_red) ?: createDefaultShape(context, style)
        }
        if (style.isEnter) {
            return ContextCompat.getDrawable(context, R.drawable.bg_enter_blue) ?: createDefaultShape(context, style)
        }
        return createDefaultShape(context, style)
    }

    private fun createDefaultShape(context: Context, style: FlorisComputedStyle): Drawable {
        val dpRadius = dpToPx(context, style.cornerRadius)
        val dpStroke = dpToPx(context, style.strokeWidth.toFloat()).toInt()

        val shape = GradientDrawable().apply {
            this.shape = GradientDrawable.RECTANGLE
            cornerRadius = dpRadius
            setColor(style.backgroundColor)
            if (dpStroke > 0) {
                setStroke(dpStroke, style.strokeColor)
            }
        }

        val mask = GradientDrawable().apply {
            this.shape = GradientDrawable.RECTANGLE
            cornerRadius = dpRadius
            setColor(Color.WHITE)
        }

        return RippleDrawable(ColorStateList.valueOf(style.backgroundPressedColor), shape, mask)
    }

    /**
     * Resolves the keyboard backplate background drawable, supporting wallpaper images,
     * scrim layers, and solid/gradient fills.
     */
    fun createKeyboardBackground(context: Context, theme: ParsedFlorisTheme): Drawable {
        if (!theme.wallpaperPath.isNullOrEmpty()) {
            val imageFile = File(theme.wallpaperPath)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (bitmap != null) {
                    val bitmapDrawable = BitmapDrawable(context.resources, bitmap)
                    val scrimColor = if (theme.isNight) Color.parseColor("#CC131316") else Color.parseColor("#99FFFFFF")
                    val scrimDrawable = ColorDrawable(scrimColor)
                    return LayerDrawable(arrayOf(bitmapDrawable, scrimDrawable))
                }
            }
        }

        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.setColor(theme.keyboardBgColor)
        return drawable
    }

    /**
     * Smartbar toolbar background.
     */
    fun createSmartbarBackground(context: Context, theme: ParsedFlorisTheme): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.setColor(theme.smartbarBgColor)
        return drawable
    }

    /**
     * Smartbar suggestion chip background.
     */
    fun createSmartbarChipDrawable(context: Context, isPrimary: Boolean, theme: ParsedFlorisTheme): Drawable {
        val dpRadius = dpToPx(context, 16f)
        val dpStroke = dpToPx(context, 1f).toInt()
        val bg = if (isPrimary) theme.accentKeyBgColor else Color.parseColor("#2AFFFFFF")
        val stroke = if (isPrimary) Color.TRANSPARENT else Color.parseColor("#33FFFFFF")

        val shape = GradientDrawable().apply {
            this.shape = GradientDrawable.RECTANGLE
            cornerRadius = dpRadius
            setColor(bg)
            if (dpStroke > 0) {
                setStroke(dpStroke, stroke)
            }
        }

        val mask = GradientDrawable().apply {
            this.shape = GradientDrawable.RECTANGLE
            cornerRadius = dpRadius
            setColor(Color.WHITE)
        }

        return RippleDrawable(ColorStateList.valueOf(theme.keyBgPressedColor), shape, mask)
    }

    /**
     * Secondary popup preview bubble background.
     */
    fun createPopupBackground(context: Context, theme: ParsedFlorisTheme): Drawable {
        val dpRadius = dpToPx(context, 12f)
        val dpStroke = dpToPx(context, 1f).toInt()
        val shape = GradientDrawable().apply {
            this.shape = GradientDrawable.RECTANGLE
            cornerRadius = dpRadius
            setColor(theme.smartbarBgColor)
            if (dpStroke > 0) {
                setStroke(dpStroke, theme.strokeColor)
            }
        }
        return shape
    }

    private fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }
}
