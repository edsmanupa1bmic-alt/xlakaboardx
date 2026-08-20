package org.fossify.keyboard.helpers

import android.content.Context
import android.util.Log
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import org.fossify.keyboard.extensions.config
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Central WhatsApp-style Emoji Provider and Rendering Engine for xLakaBoardx.
 * Handles EmojiCompat initialization, Unicode glyph processing, and emoji style synchronization.
 */
object EmojiManager {

    private const val TAG = "EmojiManager"
    private var isInitialized = false
    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    fun init(context: Context) {
        if (isInitialized) return

        try {
            val config = BundledEmojiCompatConfig(context.applicationContext)
                .setReplaceAll(true)
                .registerInitCallback(object : EmojiCompat.InitCallback() {
                    override fun onInitialized() {
                        Log.d(TAG, "WhatsApp Emoji provider successfully initialized")
                        isInitialized = true
                        notifyListeners()
                    }

                    override fun onFailed(throwable: Throwable?) {
                        Log.e(TAG, "EmojiCompat initialization failed", throwable)
                    }
                })
            EmojiCompat.init(config)
            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize BundledEmojiCompatConfig", e)
        }
    }

    /**
     * Processes any string or char sequence with the WhatsApp emoji glyph rendering engine.
     * Prevents missing glyphs (tofu boxes) and provides smooth rendering.
     */
    fun processEmoji(charSequence: CharSequence?): CharSequence {
        if (charSequence.isNullOrEmpty()) return ""
        return try {
            if (EmojiCompat.isConfigured() && EmojiCompat.get().loadState == EmojiCompat.LOAD_STATE_SUCCEEDED) {
                EmojiCompat.get().process(charSequence) ?: charSequence
            } else {
                charSequence
            }
        } catch (e: Exception) {
            charSequence
        }
    }

    fun addListener(listener: () -> Unit) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.invoke() }
    }

    fun getActiveEmojiStyle(context: Context): String {
        return context.config.emojiStyle
    }

    fun setEmojiStyle(context: Context, style: String) {
        context.config.emojiStyle = style
        notifyListeners()
    }

    fun getEmojiStyleDisplayName(style: String): String {
        return when (style) {
            EMOJI_STYLE_WHATSAPP -> "WhatsApp Style (Default)"
            EMOJI_STYLE_APPLE -> "Apple / iOS Style"
            EMOJI_STYLE_GOOGLE -> "Google / Android Style"
            EMOJI_STYLE_SYSTEM -> "System Default"
            else -> "WhatsApp Style (Default)"
        }
    }
}
