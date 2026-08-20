package org.fossify.keyboard.activities

import android.os.Bundle
import android.util.TypedValue
import org.fossify.commons.extensions.beGone
import org.fossify.commons.extensions.beVisible
import org.fossify.keyboard.databinding.ActivityPreferencesBinding
import org.fossify.keyboard.extensions.config

class PreferencesActivity : SimpleActivity() {

    private lateinit var binding: ActivityPreferencesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadPreferences()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.prefToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadPreferences() {
        binding.apply {
            // Keys
            switchNumberRow.isChecked = config.showNumbersRow
            switchNumberRowPasswords.isChecked = config.numberRowPasswords
            switchEmojiKey.isChecked = config.showEmojiKey
            switchCommaKey.isChecked = config.showCommaKey
            switchPeriodKey.isChecked = config.showPeriodKey

            // Appearance
            val fontScale = config.fontScale
            sliderFontSize.value = fontScale.toFloat().coerceIn(70f, 140f)
            fontSizePreview.text = "Tt ($fontScale%)"
            fontSizePreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16f * fontScale / 100f))

            val emojiScale = config.emojiScale
            sliderEmojiSize.value = emojiScale.toFloat().coerceIn(70f, 150f)
            emojiScaleLabel.text = "$emojiScale%"
            emojiPreviewStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, (22f * emojiScale / 100f))

            val bordersEnabled = config.keyBordersEnabled
            switchKeyBorders.isChecked = bordersEnabled
            if (bordersEnabled) keyBorderWidthContainer.beVisible() else keyBorderWidthContainer.beGone()
            sliderKeyBorderWidth.value = config.keyBorderWidth.coerceIn(0f, 1.0f)
            keyBorderWidthLabel.text = "Border Width: ${config.keyBorderWidth} dp"

            switchRgbText.isChecked = config.rgbTextEnabled

            // Layout & Shortcuts
            switchSuggestionStrip.isChecked = config.suggestionStripEnabled
            switchDoubleSpacePeriod.isChecked = config.doubleSpacePeriod
            switchAutoSpace.isChecked = config.autoSpacePunctuation
            switchTouchHoldSymbols.isChecked = config.touchHoldSymbols

            // Key tap Feedback
            switchKeyPopup.isChecked = config.showPopupOnKeypress
            val soundEnabled = config.soundOnKeypress != 0
            switchSound.isChecked = soundEnabled
            if (soundEnabled) soundVolumeContainer.beVisible() else soundVolumeContainer.beGone()
            sliderSoundVolume.value = config.keypressSoundVolume.toFloat().coerceIn(0f, 100f)
            soundVolumeLabel.text = "Volume on keypress: ${config.keypressSoundVolume}%"

            val vibrationEnabled = config.vibrateOnKeypress
            switchVibration.isChecked = vibrationEnabled
            if (vibrationEnabled) vibrationStrengthContainer.beVisible() else vibrationStrengthContainer.beGone()
            sliderVibrationStrength.value = config.keyVibrationDuration.toFloat().coerceIn(5f, 100f)
            vibrationStrengthLabel.text = "Vibration strength: ${config.keyVibrationDuration} ms"

            sliderTouchHoldDelay.value = config.touchHoldDelay.toFloat().coerceIn(150f, 700f)
            touchHoldDelayLabel.text = "Key long-press delay: ${config.touchHoldDelay} ms"
        }
    }

    private fun setupListeners() {
        binding.apply {
            // Keys
            switchNumberRow.setOnCheckedChangeListener { _, isChecked ->
                config.showNumbersRow = isChecked
            }

            switchNumberRowPasswords.setOnCheckedChangeListener { _, isChecked ->
                config.numberRowPasswords = isChecked
            }

            switchEmojiKey.setOnCheckedChangeListener { _, isChecked ->
                config.showEmojiKey = isChecked
            }

            switchCommaKey.setOnCheckedChangeListener { _, isChecked ->
                config.showCommaKey = isChecked
            }

            switchPeriodKey.setOnCheckedChangeListener { _, isChecked ->
                config.showPeriodKey = isChecked
            }

            // Appearance - Font Size
            sliderFontSize.addOnChangeListener { _, value, _ ->
                val intValue = value.toInt()
                config.fontScale = intValue
                fontSizePreview.text = "Tt ($intValue%)"
                fontSizePreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16f * intValue / 100f))
            }

            // Appearance - Emoji Size
            sliderEmojiSize.addOnChangeListener { _, value, _ ->
                val intValue = value.toInt()
                config.emojiScale = intValue
                emojiScaleLabel.text = "$intValue%"
                emojiPreviewStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, (22f * intValue / 100f))
            }

            switchKeyBorders.setOnCheckedChangeListener { _, isChecked ->
                config.keyBordersEnabled = isChecked
                if (isChecked) keyBorderWidthContainer.beVisible() else keyBorderWidthContainer.beGone()
            }

            sliderKeyBorderWidth.addOnChangeListener { _, value, _ ->
                val formattedValue = String.format("%.1f", value).toFloat()
                config.keyBorderWidth = formattedValue
                keyBorderWidthLabel.text = "Border Width: $formattedValue dp"
            }

            switchRgbText.setOnCheckedChangeListener { _, isChecked ->
                config.rgbTextEnabled = isChecked
            }

            // Layout & Shortcuts
            switchSuggestionStrip.setOnCheckedChangeListener { _, isChecked ->
                config.suggestionStripEnabled = isChecked
            }

            switchDoubleSpacePeriod.setOnCheckedChangeListener { _, isChecked ->
                config.doubleSpacePeriod = isChecked
            }

            switchAutoSpace.setOnCheckedChangeListener { _, isChecked ->
                config.autoSpacePunctuation = isChecked
            }

            switchTouchHoldSymbols.setOnCheckedChangeListener { _, isChecked ->
                config.touchHoldSymbols = isChecked
            }

            // Key tap Feedback
            switchKeyPopup.setOnCheckedChangeListener { _, isChecked ->
                config.showPopupOnKeypress = isChecked
            }

            switchSound.setOnCheckedChangeListener { _, isChecked ->
                config.soundOnKeypress = if (isChecked) 1 else 0
                if (isChecked) soundVolumeContainer.beVisible() else soundVolumeContainer.beGone()
            }

            sliderSoundVolume.addOnChangeListener { _, value, _ ->
                val intVal = value.toInt()
                config.keypressSoundVolume = intVal
                soundVolumeLabel.text = "Volume on keypress: $intVal%"
            }

            switchVibration.setOnCheckedChangeListener { _, isChecked ->
                config.vibrateOnKeypress = isChecked
                if (isChecked) vibrationStrengthContainer.beVisible() else vibrationStrengthContainer.beGone()
            }

            sliderVibrationStrength.addOnChangeListener { _, value, _ ->
                val intVal = value.toInt()
                config.keyVibrationDuration = intVal
                vibrationStrengthLabel.text = "Vibration strength: $intVal ms"
            }

            sliderTouchHoldDelay.addOnChangeListener { _, value, _ ->
                val intVal = value.toInt()
                config.touchHoldDelay = intVal
                touchHoldDelayLabel.text = "Key long-press delay: $intVal ms"
            }
        }
    }
}
