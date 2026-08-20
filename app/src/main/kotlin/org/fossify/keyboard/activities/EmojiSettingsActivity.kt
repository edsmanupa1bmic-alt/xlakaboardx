package org.fossify.keyboard.activities

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import org.fossify.keyboard.databinding.ActivityEmojiSettingsBinding
import org.fossify.keyboard.extensions.config
import org.fossify.keyboard.helpers.EmojiManager

class EmojiSettingsActivity : SimpleActivity() {

    private lateinit var binding: ActivityEmojiSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmojiSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadPreferences()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updateEmojiStyleSubtitle()
    }

    private fun setupToolbar() {
        binding.emojiSettingsToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun updateEmojiStyleSubtitle() {
        val currentStyle = config.emojiStyle
        binding.emojiStyleSubtitle.text = EmojiManager.getEmojiStyleDisplayName(currentStyle)
    }

    private fun loadPreferences() {
        binding.apply {
            // Layout
            switchEmojiFastRow.isChecked = config.emojiFastRow
            switchEmojiInSymbols.isChecked = config.emojiInSymbols
            updateEmojiStyleSubtitle()

            val emojiScale = config.emojiScale
            sliderEmojiScale.value = emojiScale.toFloat().coerceIn(70f, 150f)
            emojiSizeLabel.text = "$emojiScale%"
            val sampleEmojis = "😂  😘  😀  ❤️  😭  😎  🔥  🎉"
            emojiLivePreview.text = EmojiManager.processEmoji(sampleEmojis)
            emojiLivePreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, (22f * emojiScale / 100f))

            // Suggestions
            switchTextToEmoji.isChecked = config.textToEmoji
            switchGifSuggestions.isChecked = config.gifSuggestions
            switchShowStickers.isChecked = config.showStickers
        }
    }

    private fun setupListeners() {
        binding.apply {
            itemEmojiStyle.setOnClickListener {
                startActivity(Intent(this@EmojiSettingsActivity, EmojiStyleActivity::class.java))
            }

            switchEmojiFastRow.setOnCheckedChangeListener { _, isChecked ->
                config.emojiFastRow = isChecked
            }

            switchEmojiInSymbols.setOnCheckedChangeListener { _, isChecked ->
                config.emojiInSymbols = isChecked
            }

            sliderEmojiScale.addOnChangeListener { _, value, _ ->
                val intVal = value.toInt()
                config.emojiScale = intVal
                emojiSizeLabel.text = "$intVal%"
                emojiLivePreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, (22f * intVal / 100f))
            }

            switchTextToEmoji.setOnCheckedChangeListener { _, isChecked ->
                config.textToEmoji = isChecked
            }

            switchGifSuggestions.setOnCheckedChangeListener { _, isChecked ->
                config.gifSuggestions = isChecked
            }

            switchShowStickers.setOnCheckedChangeListener { _, isChecked ->
                config.showStickers = isChecked
            }
        }
    }
}

