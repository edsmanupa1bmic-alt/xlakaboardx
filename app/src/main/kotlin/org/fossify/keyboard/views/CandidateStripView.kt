package org.fossify.keyboard.views

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import org.fossify.keyboard.R
import org.fossify.keyboard.helpers.FlorisStyleEvaluator
import org.fossify.keyboard.helpers.ThemeEngine
import org.fossify.keyboard.models.ParsedFlorisTheme
import org.fossify.keyboard.nlp.SuggestionItem

class CandidateStripView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val scrollView: HorizontalScrollView
    private val chipsContainer: LinearLayout
    private var onSuggestionClickListener: ((SuggestionItem) -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentSuggestions: List<SuggestionItem> = emptyList()

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.view_candidate_strip, this, true)
        scrollView = root.findViewById(R.id.candidate_scroll_view)
        chipsContainer = root.findViewById(R.id.candidate_chips_container)
    }

    fun setOnSuggestionClickListener(listener: (SuggestionItem) -> Unit) {
        this.onSuggestionClickListener = listener
    }

    fun applyTheme(theme: ParsedFlorisTheme = ThemeEngine.getActiveTheme(context)) {
        background = FlorisStyleEvaluator.createSmartbarBackground(context, theme)
        if (currentSuggestions.isNotEmpty()) {
            setSuggestions(currentSuggestions)
        }
    }

    fun setSuggestions(suggestions: List<SuggestionItem>) {
        this.currentSuggestions = suggestions
        val action = Runnable {
            chipsContainer.removeAllViews()
            if (suggestions.isEmpty()) {
                visibility = View.GONE
                return@Runnable
            }

            visibility = View.VISIBLE
            val theme = ThemeEngine.getActiveTheme(context)
            background = FlorisStyleEvaluator.createSmartbarBackground(context, theme)
            val inflater = LayoutInflater.from(context)

            for (item in suggestions) {
                val chipView = inflater.inflate(R.layout.item_candidate_chip, chipsContainer, false)
                val textView = chipView.findViewById<TextView>(R.id.candidate_chip_text)

                textView.text = item.text
                textView.background = FlorisStyleEvaluator.createSmartbarChipDrawable(context, item.isPrimary, theme)
                if (item.isPrimary) {
                    textView.setTextColor(theme.accentKeyTextColor)
                } else {
                    textView.setTextColor(theme.keyTextColor)
                }

                chipView.setOnClickListener {
                    onSuggestionClickListener?.invoke(item)
                }

                chipsContainer.addView(chipView)
            }

            scrollView.scrollTo(0, 0)
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run()
        } else {
            mainHandler.post(action)
        }
    }

    fun clearSuggestions() {
        this.currentSuggestions = emptyList()
        val action = Runnable {
            chipsContainer.removeAllViews()
            visibility = View.GONE
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run()
        } else {
            mainHandler.post(action)
        }
    }
}
