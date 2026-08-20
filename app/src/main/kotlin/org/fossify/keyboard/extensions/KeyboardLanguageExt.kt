package org.fossify.keyboard.extensions

import android.annotation.SuppressLint
import android.content.Context
import org.fossify.commons.models.RadioItem
import org.fossify.keyboard.R
import org.fossify.keyboard.helpers.LANGUAGE_ENGLISH_QWERTY
import org.fossify.keyboard.helpers.LANGUAGE_SINHALA_SINGLISH
import org.fossify.keyboard.helpers.LANGUAGE_SINHALA_WIJESEKARA

fun Context.getSelectedLanguagesSorted(): List<Int> {
    return config.selectedLanguages
        .map { it to getKeyboardLanguageText(it) }
        .sortedBy { it.second }
        .map { it.first }
}

fun Context.getKeyboardLanguagesRadioItems(): ArrayList<RadioItem> {
    return getSelectedLanguagesSorted()
        .map { RadioItem(it, getKeyboardLanguageText(it)) }
        .toMutableList() as ArrayList<RadioItem>
}

@Suppress("CyclomaticComplexMethod")
fun Context.getKeyboardLanguageText(language: Int): String {
    return when (language) {
        LANGUAGE_SINHALA_SINGLISH -> "Sinhala (Singlish)"
        LANGUAGE_SINHALA_WIJESEKARA -> "Sinhala (Wijesekara)"
        LANGUAGE_ENGLISH_QWERTY -> "${getString(R.string.translation_english)} (QWERTY)"
        else -> "${getString(R.string.translation_english)} (QWERTY)"
    }
}
