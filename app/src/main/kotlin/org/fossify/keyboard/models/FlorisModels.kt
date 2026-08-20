package org.fossify.keyboard.models

import android.graphics.Color
import java.io.Serializable

data class CustomKeyboardLayout(
    val id: String,
    val name: String,
    val label: String = "",
    val direction: String = "ltr",
    val rows: List<CustomRowDef> = emptyList(),
    val isBuiltIn: Boolean = false
) : Serializable

data class CustomRowDef(
    val keys: List<CustomKeyDef> = emptyList(),
    val isNumbersRow: Boolean = false
) : Serializable

data class CustomKeyDef(
    val type: String = "character",
    val code: Int = 0,
    val label: String = "",
    val popupCharacters: List<String> = emptyList(),
    val widthWeight: Float = 1.0f,
    val topSmallNumber: String = ""
) : Serializable

data class ParsedFlorisTheme(
    val id: String,
    val name: String,
    val author: String = "Lakmal",
    val version: String = "1.0",
    val isBuiltIn: Boolean = false,
    val isNight: Boolean = true,
    val keyboardBgColor: Int = Color.parseColor("#0A195E"),
    val keyBgColor: Int = Color.parseColor("#33FFFFFF"),
    val keyBgPressedColor: Int = Color.parseColor("#55FFFFFF"),
    val keyTextColor: Int = Color.parseColor("#FFFFFF"),
    val accentKeyBgColor: Int = Color.parseColor("#1D1EFF"),
    val accentKeyTextColor: Int = Color.parseColor("#FFFFFF"),
    val smartbarBgColor: Int = Color.parseColor("#142170"),
    val smartbarTextColor: Int = Color.parseColor("#FFFFFF"),
    val strokeColor: Int = Color.parseColor("#22FFFFFF"),
    val strokeWidth: Int = 1,
    val cornerRadius: Float = 10f,
    val wallpaperPath: String? = null,
    val blurBackdrop: Boolean = true
) : Serializable
