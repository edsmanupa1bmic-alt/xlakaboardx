package org.fossify.keyboard.views

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import org.fossify.keyboard.R
import org.fossify.keyboard.helpers.FlorisStyleEvaluator
import org.fossify.keyboard.helpers.RecentEmojiManager
import org.fossify.keyboard.helpers.ThemeEngine
import org.fossify.keyboard.models.ParsedFlorisTheme

class RecentEmojiStripView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val scrollView: HorizontalScrollView
    private val emojisContainer: LinearLayout
    private var onEmojiClickListener: ((String) -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val recentEmojiListener = {
        loadEmojis()
    }

    private val emojiManagerListener = {
        loadEmojis()
    }

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.view_recent_emojis, this, true)
        scrollView = root.findViewById(R.id.recent_emojis_scroll)
        emojisContainer = root.findViewById(R.id.recent_emojis_container)
        loadEmojis()
    }

    fun setOnEmojiClickListener(listener: (String) -> Unit) {
        this.onEmojiClickListener = listener
    }

    fun applyTheme(theme: ParsedFlorisTheme = ThemeEngine.getActiveTheme(context)) {
        background = FlorisStyleEvaluator.createSmartbarBackground(context, theme)
        loadEmojis()
    }

    fun loadEmojis() {
        val action = Runnable {
            val recentEmojiManager = RecentEmojiManager.getInstance(context)
            val emojis = recentEmojiManager.getRecentEmojis()
            val inflater = LayoutInflater.from(context)

            emojisContainer.removeAllViews()
            val theme = ThemeEngine.getActiveTheme(context)
            background = FlorisStyleEvaluator.createSmartbarBackground(context, theme)

            for (emoji in emojis) {
                val chipView = inflater.inflate(R.layout.item_recent_emoji_chip, emojisContainer, false)
                val textView = chipView.findViewById<TextView>(R.id.recent_emoji_text)
                textView.text = org.fossify.keyboard.helpers.EmojiManager.processEmoji(emoji)
                textView.background = FlorisStyleEvaluator.createSmartbarChipDrawable(context, false, theme)

                chipView.setOnClickListener {
                    onEmojiClickListener?.invoke(emoji)
                }

                emojisContainer.addView(chipView)
            }
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run()
        } else {
            mainHandler.post(action)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        RecentEmojiManager.getInstance(context).addListener(recentEmojiListener)
        org.fossify.keyboard.helpers.EmojiManager.addListener(emojiManagerListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        RecentEmojiManager.getInstance(context).removeListener(recentEmojiListener)
        org.fossify.keyboard.helpers.EmojiManager.removeListener(emojiManagerListener)
    }
}
