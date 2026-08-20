package org.fossify.keyboard.helpers


enum class ShiftState {
    OFF,
    ON_ONE_CHAR,
    ON_PERMANENT;
}

// limit the count of alternative characters that show up at long pressing a key
const val MAX_KEYS_PER_MINI_ROW = 9

// shared prefs
const val VIBRATE_ON_KEYPRESS = "vibrate_on_keypress"

const val SOUND_ON_KEYPRESS = "sound_on_keypress"
const val SOUND_NONE = 0
const val SOUND_SYSTEM = 1
const val SOUND_ALWAYS = 2

const val SHOW_POPUP_ON_KEYPRESS = "show_popup_on_keypress"
const val SHOW_KEY_BORDERS = "show_key_borders"
const val SENTENCES_CAPITALIZATION = "sentences_capitalization"
const val SMART_AUTO_CORRECTION = "smart_auto_correction"
const val SHOW_EMOJI_KEY = "show_emoji_key"
const val SHOW_LANGUAGE_SWITCH_KEY = "show_language_switch_key"
const val LAST_EXPORTED_CLIPS_FOLDER = "last_exported_clips_folder"
const val KEYBOARD_LANGUAGE = "keyboard_language"
const val HEIGHT_PERCENTAGE = "height_percentage"
const val SHOW_CLIPBOARD_CONTENT = "show_clipboard_content"
const val SHOW_NUMBERS_ROW = "show_numbers_row"
const val SELECTED_LANGUAGES = "selected_languages"
const val VOICE_INPUT_METHOD = "voice_input_method"
const val RECENTLY_USED_EMOJIS = "recently_used_emojis"
const val EMOJI_STYLE = "emoji_style"
const val EMOJI_STYLE_WHATSAPP = "whatsapp"
const val EMOJI_STYLE_APPLE = "apple"
const val EMOJI_STYLE_GOOGLE = "google"
const val EMOJI_STYLE_SYSTEM = "system"

// differentiate current and pinned clips at the keyboards' Clipboard section
const val ITEM_SECTION_LABEL = 0
const val ITEM_CLIP = 1

const val LANGUAGE_ENGLISH_QWERTY = 0
const val LANGUAGE_RUSSIAN = 1
const val LANGUAGE_FRENCH_AZERTY = 2
const val LANGUAGE_ENGLISH_QWERTZ = 3
const val LANGUAGE_SPANISH = 4
const val LANGUAGE_GERMAN = 5
const val LANGUAGE_ENGLISH_DVORAK = 6
const val LANGUAGE_ROMANIAN = 7
const val LANGUAGE_SLOVENIAN = 8
const val LANGUAGE_BULGARIAN = 9
const val LANGUAGE_TURKISH_Q = 10
const val LANGUAGE_LITHUANIAN = 11
const val LANGUAGE_BENGALI = 12
const val LANGUAGE_GREEK = 13
const val LANGUAGE_NORWEGIAN = 14
const val LANGUAGE_SWEDISH = 15
const val LANGUAGE_DANISH = 16
const val LANGUAGE_FRENCH_BEPO = 17
const val LANGUAGE_VIETNAMESE_TELEX = 18
const val LANGUAGE_POLISH = 19
const val LANGUAGE_UKRAINIAN = 20
const val LANGUAGE_CHUVASH = 22
const val LANGUAGE_ESPERANTO = 23
const val LANGUAGE_HEBREW = 24
const val LANGUAGE_ARABIC = 25
const val LANGUAGE_CENTRAL_KURDISH = 26
const val LANGUAGE_BELARUSIAN_CYRL = 27
const val LANGUAGE_BELARUSIAN_LATN = 28
const val LANGUAGE_KABYLE_AZERTY = 29
const val LANGUAGE_CZECH_QWERTY = 30
const val LANGUAGE_ITALIAN = 31
const val LANGUAGE_CZECH_QWERTZ = 32
const val LANGUAGE_GERMAN_QWERTZ = 33
const val LANGUAGE_PORTUGUESE = 34
const val LANGUAGE_PORTUGUESE_HCESAR = 35
const val LANGUAGE_DUTCH = 36
const val LANGUAGE_LATVIAN = 37
const val LANGUAGE_TURKISH = 38
const val LANGUAGE_ENGLISH_ASSET = 39
const val LANGUAGE_ENGLISH_COLEMAK = 40
const val LANGUAGE_ENGLISH_COLEMAKDH = 41
const val LANGUAGE_ENGLISH_NIRO = 42
const val LANGUAGE_ENGLISH_SOUL = 43
const val LANGUAGE_ENGLISH_WORKMAN = 44
const val LANGUAGE_SINHALA_SINGLISH = 45
const val LANGUAGE_SINHALA_WIJESEKARA = 46
const val LANGUAGE_CUSTOM_LAYOUT = 100

const val ACTION_THEME_CHANGED = "org.fossify.keyboard.ACTION_THEME_CHANGED"
const val ACTION_LAYOUT_CHANGED = "org.fossify.keyboard.ACTION_LAYOUT_CHANGED"

// Keep this sorted
val SUPPORTED_LANGUAGES = listOf(
    LANGUAGE_ENGLISH_QWERTY,
    LANGUAGE_SINHALA_SINGLISH,
    LANGUAGE_SINHALA_WIJESEKARA,
    LANGUAGE_CUSTOM_LAYOUT
)

// keyboard height percentage options
const val KEYBOARD_HEIGHT_70_PERCENT = 70
const val KEYBOARD_HEIGHT_80_PERCENT = 80
const val KEYBOARD_HEIGHT_90_PERCENT = 90
const val KEYBOARD_HEIGHT_100_PERCENT = 100
const val KEYBOARD_HEIGHT_120_PERCENT = 120
const val KEYBOARD_HEIGHT_140_PERCENT = 140
const val KEYBOARD_HEIGHT_160_PERCENT = 160

const val EMOJI_SPEC_FILE_PATH = "media/emoji_spec.txt"
const val LANGUAGE_VN_TELEX = "language/extension.json"
const val RECENT_EMOJIS_LIMIT = 36

// Android constant
const val INPUT_METHOD_SUBTYPE_VOICE = "voice"

// Preference keys
const val NUMBER_ROW_PASSWORDS = "number_row_passwords"
const val SHOW_COMMA_KEY = "show_comma_key"
const val SHOW_PERIOD_KEY = "show_period_key"
const val FONT_SCALE = "font_scale"
const val EMOJI_SCALE = "emoji_scale"
const val SUGGESTION_STRIP_ENABLED = "suggestion_strip_enabled"
const val ONE_HANDED_MODE = "one_handed_mode"
const val DOUBLE_SPACE_PERIOD = "double_space_period"
const val AUTO_SPACE_PUNCTUATION = "auto_space_punctuation"
const val TOUCH_HOLD_SYMBOLS = "touch_hold_symbols"
const val KEY_VIBRATION_DURATION = "key_vibration_duration"
const val KEYPRESS_SOUND_VOLUME = "keypress_sound_volume"
const val TOUCH_HOLD_DELAY = "touch_hold_delay"
const val SPELL_CHECK_ENABLED = "spell_check_enabled"
const val GRAMMAR_CHECK_ENABLED = "grammar_check_enabled"
const val BLOCK_OFFENSIVE_WORDS = "block_offensive_words"
const val NEXT_WORD_SUGGESTIONS = "next_word_suggestions"
const val SUGGEST_CONTACTS = "suggest_contacts"
const val EMOJI_FAST_ROW = "emoji_fast_row"
const val EMOJI_IN_SYMBOLS = "emoji_in_symbols"
const val TEXT_TO_EMOJI = "text_to_emoji"
const val GIF_SUGGESTIONS = "gif_suggestions"
const val SHOW_STICKERS = "show_stickers"
const val APP_ICON_IN_LAUNCHER = "app_icon_in_launcher"

