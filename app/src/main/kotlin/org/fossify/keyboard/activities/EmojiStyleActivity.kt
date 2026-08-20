package org.fossify.keyboard.activities

import android.os.Bundle
import org.fossify.commons.extensions.beGone
import org.fossify.commons.extensions.beVisible
import org.fossify.commons.extensions.toast
import org.fossify.keyboard.databinding.ActivityEmojiStyleBinding
import org.fossify.keyboard.extensions.config
import org.fossify.keyboard.helpers.EMOJI_STYLE_APPLE
import org.fossify.keyboard.helpers.EMOJI_STYLE_GOOGLE
import org.fossify.keyboard.helpers.EMOJI_STYLE_SYSTEM
import org.fossify.keyboard.helpers.EMOJI_STYLE_WHATSAPP
import org.fossify.keyboard.helpers.EmojiManager

class EmojiStyleActivity : SimpleActivity() {

    private lateinit var binding: ActivityEmojiStyleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmojiStyleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdge(padBottomSystem = listOf(binding.emojiStyleScrollview))
        setupToolbar()
        loadPreviews()
        updateSelectionUI(config.emojiStyle)
        setupListeners()
    }

    private fun setupToolbar() {
        binding.emojiStyleToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadPreviews() {
        val sample = "😍 🔥 ❤️ 😂 👍 🎉 ✨ 🚀"
        val processedSample = EmojiManager.processEmoji(sample)
        binding.apply {
            previewWhatsapp.text = processedSample
            previewApple.text = processedSample
            previewGoogle.text = processedSample
            previewSystem.text = sample
        }
    }

    private fun updateSelectionUI(selectedStyle: String) {
        binding.apply {
            checkStyleWhatsapp.beGone()
            checkStyleApple.beGone()
            checkStyleGoogle.beGone()
            checkStyleSystem.beGone()

            when (selectedStyle) {
                EMOJI_STYLE_WHATSAPP -> checkStyleWhatsapp.beVisible()
                EMOJI_STYLE_APPLE -> checkStyleApple.beVisible()
                EMOJI_STYLE_GOOGLE -> checkStyleGoogle.beVisible()
                EMOJI_STYLE_SYSTEM -> checkStyleSystem.beVisible()
                else -> checkStyleWhatsapp.beVisible()
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            itemStyleWhatsapp.setOnClickListener {
                selectStyle(EMOJI_STYLE_WHATSAPP, "WhatsApp Emoji Pack applied")
            }

            itemStyleApple.setOnClickListener {
                selectStyle(EMOJI_STYLE_APPLE, "Apple / iOS Emoji style applied")
            }

            itemStyleGoogle.setOnClickListener {
                selectStyle(EMOJI_STYLE_GOOGLE, "Google Emoji style applied")
            }

            itemStyleSystem.setOnClickListener {
                selectStyle(EMOJI_STYLE_SYSTEM, "Device Default Emoji style applied")
            }

            btnResetEmojiStyle.setOnClickListener {
                selectStyle(EMOJI_STYLE_WHATSAPP, "Reset to WhatsApp default emoji style")
            }
        }
    }

    private fun selectStyle(style: String, message: String) {
        config.emojiStyle = style
        EmojiManager.setEmojiStyle(this, style)
        updateSelectionUI(style)
        toast(message)
    }
}
