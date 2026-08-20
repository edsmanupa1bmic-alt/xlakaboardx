package org.fossify.keyboard.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import java.io.File

object FlashboardThemeEngine {
    
    fun getTranslucentKeyDrawable(context: Context, normalAlpha: Int): StateListDrawable {
        val normalDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f * context.resources.displayMetrics.density
            setColor(Color.argb(normalAlpha.coerceIn(0, 255), 255, 255, 255))
            setStroke(0, Color.TRANSPARENT)
        }

        val pressedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f * context.resources.displayMetrics.density
            setColor(Color.argb(0x40, 255, 255, 255)) // #40FFFFFF
        }

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
        stateListDrawable.addState(intArrayOf(), normalDrawable)
        return stateListDrawable
    }

    fun getSpacebarDrawable(context: Context): StateListDrawable {
        val normalDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f * context.resources.displayMetrics.density
            setColor(Color.parseColor("#990006"))
        }

        val pressedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f * context.resources.displayMetrics.density
            setColor(Color.parseColor("#7A0005"))
        }

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
        stateListDrawable.addState(intArrayOf(), normalDrawable)
        return stateListDrawable
    }

    fun getEnterKeyDrawable(context: Context): StateListDrawable {
        val normalDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f * context.resources.displayMetrics.density
            setColor(Color.parseColor("#0066FF"))
        }

        val pressedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f * context.resources.displayMetrics.density
            setColor(Color.parseColor("#0052CC"))
        }

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
        stateListDrawable.addState(intArrayOf(), normalDrawable)
        return stateListDrawable
    }

    fun getUtilityChipDrawable(context: Context): StateListDrawable {
        val normalDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#33FFFFFF"))
        }

        val pressedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#55FFFFFF"))
        }

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
        stateListDrawable.addState(intArrayOf(), normalDrawable)
        return stateListDrawable
    }

    fun getWallpaperBitmap(context: Context): Bitmap? {
        val themesDir = File(context.filesDir, "themes")
        val bgFile = File(themesDir, "active_wallpaper.png")
        if (!bgFile.exists()) return null

        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.HARDWARE
        }
        return BitmapFactory.decodeFile(bgFile.absolutePath, options)
    }
}
