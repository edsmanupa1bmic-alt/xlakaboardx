package org.fossify.keyboard.activities

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import org.fossify.commons.extensions.beGone
import org.fossify.commons.extensions.beVisible
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.updateTextColors
import org.fossify.commons.extensions.viewBinding
import org.fossify.keyboard.R
import org.fossify.keyboard.databinding.ActivityLayoutsBinding
import org.fossify.keyboard.extensions.config
import org.fossify.keyboard.helpers.CustomLayoutManager
import org.fossify.keyboard.helpers.LANGUAGE_ENGLISH_QWERTY
import org.fossify.keyboard.helpers.LANGUAGE_SINHALA_SINGLISH
import org.fossify.keyboard.helpers.LANGUAGE_SINHALA_WIJESEKARA

class LayoutsActivity : SimpleActivity() {

    private val binding by viewBinding(ActivityLayoutsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupEdgeToEdge(padBottomSystem = listOf(binding.layoutsNestedScrollview))
        setupMaterialScrollListener(binding.layoutsNestedScrollview, binding.layoutsAppbar)
        setupTopAppBar(binding.layoutsAppbar)

        refreshLayouts()
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(binding.layoutsNestedScrollview)
        binding.standardLayoutsLabel.setTextColor(getProperPrimaryColor())
    }

    private fun refreshLayouts() {
        val currentLang = config.keyboardLanguage

        // 1. Standard Built-in Layouts
        binding.standardLayoutsHolder.removeAllViews()
        val standardList = listOf(
            Triple(LANGUAGE_SINHALA_SINGLISH, "Sinhala (Singlish Phonetic)", "Real-time English to Sinhala conversion"),
            Triple(LANGUAGE_SINHALA_WIJESEKARA, "Sinhala (Wijesekara)", "Traditional Standard Wijesekara Layout"),
            Triple(LANGUAGE_ENGLISH_QWERTY, "English (QWERTY)", "Standard English Latin QWERTY")
        )

        for ((langCode, title, desc) in standardList) {
            val isSelected = currentLang == langCode
            val cardView = createStandardCard(title, desc, isSelected) {
                config.keyboardLanguage = langCode
                CustomLayoutManager.setActiveLayoutId(this, null)
                refreshLayouts()
            }
            binding.standardLayoutsHolder.addView(cardView)
        }
    }

    private fun createStandardCard(title: String, desc: String, isActive: Boolean, onClick: () -> Unit): View {
        val view = LayoutInflater.from(this).inflate(R.layout.item_layout_card, null, false)
        val rootCard = view.findViewById<MaterialCardView>(R.id.layout_card_root)
        val titleTv = view.findViewById<TextView>(R.id.layout_title)
        val subtitleTv = view.findViewById<TextView>(R.id.layout_subtitle)
        val activeBadge = view.findViewById<ImageView>(R.id.layout_active_badge)
        val deleteBtn = view.findViewById<ImageView>(R.id.layout_delete_button)

        titleTv.text = title
        subtitleTv.text = desc
        deleteBtn.beGone()

        if (isActive) {
            activeBadge.beVisible()
            activeBadge.imageTintList = ColorStateList.valueOf(getProperPrimaryColor())
            rootCard.strokeColor = getProperPrimaryColor()
            rootCard.strokeWidth = 3
        } else {
            activeBadge.beGone()
            rootCard.strokeColor = Color.parseColor("#22FFFFFF")
            rootCard.strokeWidth = 1
        }

        rootCard.setOnClickListener { onClick() }
        return view
    }
}
