package org.fossify.keyboard.helpers

import android.content.Context
import org.fossify.commons.helpers.BaseConfig
import org.fossify.keyboard.extensions.isDeviceLocked
import org.fossify.keyboard.extensions.safeStorageContext
import java.util.Locale

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context.safeStorageContext)
    }

    var vibrateOnKeypress: Boolean
        get() = prefs.getBoolean(VIBRATE_ON_KEYPRESS, true)
        set(vibrateOnKeypress) = prefs.edit().putBoolean(VIBRATE_ON_KEYPRESS, vibrateOnKeypress).apply()

    var soundOnKeypress: Int
        get() = prefs.getInt(SOUND_ON_KEYPRESS, SOUND_SYSTEM)
        set(soundOnKeypress) = prefs.edit().putInt(SOUND_ON_KEYPRESS, soundOnKeypress).apply()

    var showPopupOnKeypress: Boolean
        get() = prefs.getBoolean(SHOW_POPUP_ON_KEYPRESS, true)
        set(showPopupOnKeypress) = prefs.edit().putBoolean(SHOW_POPUP_ON_KEYPRESS, showPopupOnKeypress).apply()

    var enableSentencesCapitalization: Boolean
        get() = prefs.getBoolean(SENTENCES_CAPITALIZATION, true)
        set(enableCapitalization) = prefs.edit().putBoolean(SENTENCES_CAPITALIZATION, enableCapitalization).apply()

    var smartAutoCorrection: Boolean
        get() = prefs.getBoolean(SMART_AUTO_CORRECTION, true)
        set(smartAutoCorrection) = prefs.edit().putBoolean(SMART_AUTO_CORRECTION, smartAutoCorrection).apply()

    var showEmojiKey: Boolean
        get() = prefs.getBoolean(SHOW_EMOJI_KEY, false)
        set(showEmojiKey) = prefs.edit().putBoolean(SHOW_EMOJI_KEY, showEmojiKey).apply()

    var showLanguageSwitchKey: Boolean
        get() = prefs.getBoolean(SHOW_LANGUAGE_SWITCH_KEY, true)
        set(showLanguageSwitchKey) = prefs.edit().putBoolean(SHOW_LANGUAGE_SWITCH_KEY, showLanguageSwitchKey).apply()

    var showKeyBorders: Boolean
        get() = prefs.getBoolean(SHOW_KEY_BORDERS, true)
        set(showKeyBorders) = prefs.edit().putBoolean(SHOW_KEY_BORDERS, showKeyBorders).apply()

    var keyBordersEnabled: Boolean
        get() = prefs.getBoolean("key_borders_enabled", false)
        set(value) = prefs.edit().putBoolean("key_borders_enabled", value).apply()

    var keyBorderWidth: Float
        get() = prefs.getFloat("key_border_width", 0.0f)
        set(value) = prefs.edit().putFloat("key_border_width", value).apply()

    var rgbTextEnabled: Boolean
        get() = prefs.getBoolean("rgb_text_enabled", false)
        set(value) = prefs.edit().putBoolean("rgb_text_enabled", value).apply()

    var bgImageEnabled: Boolean
        get() = prefs.getBoolean("bg_image_enabled", false)
        set(value) = prefs.edit().putBoolean("bg_image_enabled", value).apply()

    var bgDimOpacity: Float
        get() = prefs.getFloat("bg_dim_opacity", 0.5f)
        set(value) = prefs.edit().putFloat("bg_dim_opacity", value).apply()

    var bgBlurRadius: Float
        get() = prefs.getFloat("bg_blur_radius", 0f)
        set(value) = prefs.edit().putFloat("bg_blur_radius", value).apply()

    var keyTranslucencyAlpha: Int
        get() = prefs.getInt("key_translucency_alpha", 30) // Default some translucency
        set(value) = prefs.edit().putInt("key_translucency_alpha", value).apply()

    var customSpacebarText: String
        get() = prefs.getString("custom_spacebar_text", "xLakaBoardx") ?: "xLakaBoardx"
        set(value) = prefs.edit().putString("custom_spacebar_text", value).apply()

    var keyTextColor: Int
        get() = prefs.getInt("key_text_color", android.graphics.Color.WHITE)
        set(value) = prefs.edit().putInt("key_text_color", value).apply()

    var spacebarColor: Int
        get() = prefs.getInt("spacebar_color", android.graphics.Color.parseColor("#990006"))
        set(value) = prefs.edit().putInt("spacebar_color", value).apply()

    var activePresetTheme: String
        get() = prefs.getString("active_preset_theme", "Deep AMOLED") ?: "Deep AMOLED"
        set(value) = prefs.edit().putString("active_preset_theme", value).apply()

    var lastExportedClipsFolder: String
        get() = prefs.getString(LAST_EXPORTED_CLIPS_FOLDER, "")!!
        set(lastExportedClipsFolder) = prefs.edit().putString(LAST_EXPORTED_CLIPS_FOLDER, lastExportedClipsFolder).apply()

    var keyboardLanguage: Int
        get() = prefs.getInt(KEYBOARD_LANGUAGE, getDefaultLanguage())
        set(keyboardLanguage) = prefs.edit().putInt(KEYBOARD_LANGUAGE, keyboardLanguage).apply()

    var keyboardHeightPercentage: Int
        get() = prefs.getInt(HEIGHT_PERCENTAGE, 100)
        set(keyboardHeightMultiplier) = prefs.edit().putInt(HEIGHT_PERCENTAGE, keyboardHeightMultiplier).apply()

    var showClipboardContent: Boolean
        get() = prefs.getBoolean(SHOW_CLIPBOARD_CONTENT, true)
        set(showClipboardContent) = prefs.edit().putBoolean(SHOW_CLIPBOARD_CONTENT, showClipboardContent).apply()

    var showNumbersRow: Boolean
        get() = if (context.isDeviceLocked) {
            true
        } else {
            prefs.getBoolean(SHOW_NUMBERS_ROW, false)
        }
        set(showNumbersRow) = prefs.edit().putBoolean(SHOW_NUMBERS_ROW, showNumbersRow).apply()

    var voiceInputMethod: String
        get() = prefs.getString(VOICE_INPUT_METHOD, "")!!
        set(voiceInputMethod) = prefs.edit().putString(VOICE_INPUT_METHOD, voiceInputMethod).apply()

    var selectedLanguages: MutableSet<Int>
        get() {
            val defaultLanguage = getDefaultLanguage().toString()
            val stringSet = prefs.getStringSet(SELECTED_LANGUAGES, hashSetOf(defaultLanguage))!!
            return stringSet.map { it.toInt() }.toMutableSet()
        }
        set(selectedLanguages) {
            val stringSet = selectedLanguages.map { it.toString() }.toSet()
            prefs.edit().putStringSet(SELECTED_LANGUAGES, stringSet).apply()
        }

    fun getDefaultLanguage(): Int {
        val conf = context.resources.configuration
        return if (conf.locale.toString().lowercase(Locale.getDefault()).startsWith("ru_")) {
            LANGUAGE_RUSSIAN
        } else {
            LANGUAGE_ENGLISH_QWERTY
        }
    }

    var emojiStyle: String
        get() = prefs.getString(EMOJI_STYLE, EMOJI_STYLE_WHATSAPP) ?: EMOJI_STYLE_WHATSAPP
        set(value) = prefs.edit().putString(EMOJI_STYLE, value).apply()

    var recentlyUsedEmojis: List<String>
        get() = prefs.getString(RECENTLY_USED_EMOJIS, "❤️|😂|😊|😏|😒|😌|🥺|🥲|😮\u200D💨|😁")!!.split("|").filter { it.isNotEmpty() }
        set(recentlyUsedEmojis) = prefs.edit().putString(
            RECENTLY_USED_EMOJIS, recentlyUsedEmojis.joinToString("|")
        ).apply()

    fun addRecentEmoji(emoji: String) {
        val recentEmojis = recentlyUsedEmojis.toMutableList()
        recentEmojis.remove(emoji)
        recentEmojis.add(0, emoji)
        recentlyUsedEmojis = recentEmojis.take(RECENT_EMOJIS_LIMIT)
    }

    var numberRowPasswords: Boolean
        get() = prefs.getBoolean(NUMBER_ROW_PASSWORDS, true)
        set(value) = prefs.edit().putBoolean(NUMBER_ROW_PASSWORDS, value).apply()

    var showCommaKey: Boolean
        get() = prefs.getBoolean(SHOW_COMMA_KEY, true)
        set(value) = prefs.edit().putBoolean(SHOW_COMMA_KEY, value).apply()

    var showPeriodKey: Boolean
        get() = prefs.getBoolean(SHOW_PERIOD_KEY, true)
        set(value) = prefs.edit().putBoolean(SHOW_PERIOD_KEY, value).apply()

    var fontScale: Int
        get() = prefs.getInt(FONT_SCALE, 100)
        set(value) = prefs.edit().putInt(FONT_SCALE, value).apply()

    var emojiScale: Int
        get() = prefs.getInt(EMOJI_SCALE, 100)
        set(value) = prefs.edit().putInt(EMOJI_SCALE, value).apply()

    var suggestionStripEnabled: Boolean
        get() = prefs.getBoolean(SUGGESTION_STRIP_ENABLED, true)
        set(value) = prefs.edit().putBoolean(SUGGESTION_STRIP_ENABLED, value).apply()

    var oneHandedMode: Int
        get() = prefs.getInt(ONE_HANDED_MODE, 0)
        set(value) = prefs.edit().putInt(ONE_HANDED_MODE, value).apply()

    var doubleSpacePeriod: Boolean
        get() = prefs.getBoolean(DOUBLE_SPACE_PERIOD, true)
        set(value) = prefs.edit().putBoolean(DOUBLE_SPACE_PERIOD, value).apply()

    var autoSpacePunctuation: Boolean
        get() = prefs.getBoolean(AUTO_SPACE_PUNCTUATION, false)
        set(value) = prefs.edit().putBoolean(AUTO_SPACE_PUNCTUATION, value).apply()

    var touchHoldSymbols: Boolean
        get() = prefs.getBoolean(TOUCH_HOLD_SYMBOLS, true)
        set(value) = prefs.edit().putBoolean(TOUCH_HOLD_SYMBOLS, value).apply()

    var keyVibrationDuration: Int
        get() = prefs.getInt(KEY_VIBRATION_DURATION, 20)
        set(value) = prefs.edit().putInt(KEY_VIBRATION_DURATION, value).apply()

    var keypressSoundVolume: Int
        get() = prefs.getInt(KEYPRESS_SOUND_VOLUME, 50)
        set(value) = prefs.edit().putInt(KEYPRESS_SOUND_VOLUME, value).apply()

    var touchHoldDelay: Int
        get() = prefs.getInt(TOUCH_HOLD_DELAY, 300)
        set(value) = prefs.edit().putInt(TOUCH_HOLD_DELAY, value).apply()

    var spellCheckEnabled: Boolean
        get() = prefs.getBoolean(SPELL_CHECK_ENABLED, true)
        set(value) = prefs.edit().putBoolean(SPELL_CHECK_ENABLED, value).apply()

    var grammarCheckEnabled: Boolean
        get() = prefs.getBoolean(GRAMMAR_CHECK_ENABLED, true)
        set(value) = prefs.edit().putBoolean(GRAMMAR_CHECK_ENABLED, value).apply()

    var blockOffensiveWords: Boolean
        get() = prefs.getBoolean(BLOCK_OFFENSIVE_WORDS, true)
        set(value) = prefs.edit().putBoolean(BLOCK_OFFENSIVE_WORDS, value).apply()

    var nextWordSuggestions: Boolean
        get() = prefs.getBoolean(NEXT_WORD_SUGGESTIONS, true)
        set(value) = prefs.edit().putBoolean(NEXT_WORD_SUGGESTIONS, value).apply()

    var suggestContacts: Boolean
        get() = prefs.getBoolean(SUGGEST_CONTACTS, false)
        set(value) = prefs.edit().putBoolean(SUGGEST_CONTACTS, value).apply()

    var emojiFastRow: Boolean
        get() = prefs.getBoolean(EMOJI_FAST_ROW, true)
        set(value) = prefs.edit().putBoolean(EMOJI_FAST_ROW, value).apply()

    var emojiInSymbols: Boolean
        get() = prefs.getBoolean(EMOJI_IN_SYMBOLS, true)
        set(value) = prefs.edit().putBoolean(EMOJI_IN_SYMBOLS, value).apply()

    var textToEmoji: Boolean
        get() = prefs.getBoolean(TEXT_TO_EMOJI, true)
        set(value) = prefs.edit().putBoolean(TEXT_TO_EMOJI, value).apply()

    var gifSuggestions: Boolean
        get() = prefs.getBoolean(GIF_SUGGESTIONS, true)
        set(value) = prefs.edit().putBoolean(GIF_SUGGESTIONS, value).apply()

    var showStickers: Boolean
        get() = prefs.getBoolean(SHOW_STICKERS, true)
        set(value) = prefs.edit().putBoolean(SHOW_STICKERS, value).apply()

    var appIconInLauncher: Boolean
        get() = prefs.getBoolean(APP_ICON_IN_LAUNCHER, true)
        set(value) = prefs.edit().putBoolean(APP_ICON_IN_LAUNCHER, value).apply()
}
