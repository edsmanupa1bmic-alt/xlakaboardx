package org.fossify.keyboard.activities

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import org.fossify.commons.dialogs.ConfirmationAdvancedDialog
import org.fossify.commons.extensions.beGone
import org.fossify.commons.extensions.beVisible
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.toast
import org.fossify.commons.extensions.updateTextColors
import org.fossify.commons.extensions.viewBinding
import org.fossify.keyboard.R
import org.fossify.keyboard.databinding.ActivityThemesBinding
import org.fossify.keyboard.dialogs.ThemePreviewFragment
import org.fossify.keyboard.helpers.ThemeEngine
import org.fossify.keyboard.models.ParsedFlorisTheme

class ThemesActivity : SimpleActivity() {

    private val binding by viewBinding(ActivityThemesBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupEdgeToEdge(padBottomSystem = listOf(binding.themesNestedScrollview))
        setupMaterialScrollListener(binding.themesNestedScrollview, binding.themesAppbar)
        setupTopAppBar(binding.themesAppbar)

        binding.previewKeyboardContainer.setOnClickListener {
            val activeTheme = ThemeEngine.getActiveTheme(this)
            showThemePreview(activeTheme.id)
        }

        refreshThemes()
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(binding.themesNestedScrollview)
        binding.themesPreviewLabel.setTextColor(getProperPrimaryColor())
        binding.presetThemesLabel.setTextColor(getProperPrimaryColor())
    }

    private fun showThemePreview(themeId: String) {
        val existingFragment = supportFragmentManager.findFragmentByTag("theme_preview")
        if (existingFragment == null) {
            ThemePreviewFragment.newInstance(themeId) {
                refreshThemes()
            }.show(supportFragmentManager, "theme_preview")
        }
    }

    private fun refreshThemes() {
        val activeTheme = ThemeEngine.getActiveTheme(this)
        updateLivePreview(activeTheme)

        // Populate presets
        binding.presetThemesHolder.removeAllViews()
        for (theme in ThemeEngine.builtInThemes) {
            val cardView = createThemeCard(theme, activeTheme.id == theme.id)
            binding.presetThemesHolder.addView(cardView)
        }
    }

    private fun updateLivePreview(theme: ParsedFlorisTheme) {
        binding.apply {
            previewKeyboardContainer.background = ThemeEngine.getKeyboardBackground(this@ThemesActivity, theme)
            previewSmartbar.background = ThemeEngine.getSmartbarBackground(this@ThemesActivity, theme)

            // Chips
            previewChip1.background = ThemeEngine.getCandidateChipBackground(this@ThemesActivity, isPrimary = true, theme)
            previewChip1.setTextColor(theme.accentKeyTextColor)

            previewChip2.background = ThemeEngine.getCandidateChipBackground(this@ThemesActivity, isPrimary = false, theme)
            previewChip2.setTextColor(theme.keyTextColor)

            previewChip3.background = ThemeEngine.getCandidateChipBackground(this@ThemesActivity, isPrimary = false, theme)
            previewChip3.setTextColor(theme.keyTextColor)

            // Recent Emojis
            val emojiViews = listOf(previewEmoji1, previewEmoji2, previewEmoji3, previewEmoji4, previewEmoji5)
            for (ev in emojiViews) {
                ev.background = ThemeEngine.getRecentEmojiChipBackground(this@ThemesActivity, theme)
            }

            // Normal Keys
            val keyViews = listOf(
                previewKeyQ, previewKeyW, previewKeyE, previewKeyR,
                previewKeyT, previewKeyY, previewKeyU, previewKeyI,
                previewKeyO, previewKeyP, previewKeySymbols, previewKeySpace
            )
            for (kv in keyViews) {
                kv.background = ThemeEngine.getKeyDrawable(this@ThemesActivity, isAction = false, theme)
                kv.setTextColor(theme.keyTextColor)
            }

            // Action Key (Enter)
            previewKeyEnter.background = ThemeEngine.getKeyDrawable(this@ThemesActivity, isAction = true, theme)
            previewKeyEnter.setTextColor(theme.accentKeyTextColor)
        }
    }

    private fun createThemeCard(theme: ParsedFlorisTheme, isActive: Boolean): View {
        val view = LayoutInflater.from(this).inflate(R.layout.item_theme_card, null, false)
        val rootCard = view.findViewById<MaterialCardView>(R.id.theme_card_root)
        val swatchBox = view.findViewById<View>(R.id.theme_swatch_box)
        val swatchKey = view.findViewById<View>(R.id.swatch_key)
        val swatchAccent = view.findViewById<View>(R.id.swatch_accent)
        val titleTv = view.findViewById<TextView>(R.id.theme_title)
        val subtitleTv = view.findViewById<TextView>(R.id.theme_subtitle)
        val activeBadge = view.findViewById<ImageView>(R.id.theme_active_badge)
        val deleteBtn = view.findViewById<ImageView>(R.id.theme_delete_button)

        titleTv.text = theme.name
        subtitleTv.text = if (theme.isBuiltIn) "${theme.author} • Preset" else "${theme.author} • FlorisBoard"

        // Swatch styling
        val swatchBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(theme.keyboardBgColor)
            setStroke(2, theme.strokeColor)
        }
        swatchBox.background = swatchBg

        val keySwatchBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 4f
            setColor(theme.keyBgColor)
        }
        swatchKey.background = keySwatchBg

        val accentSwatchBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 4f
            setColor(theme.accentKeyBgColor)
        }
        swatchAccent.background = accentSwatchBg

        // Active state
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

        // Delete button for custom themes (hidden as FlorisBoard import is disabled)
        deleteBtn.beGone()

        rootCard.setOnClickListener {
            // Show preview mockup fragment for the selected theme before final application
            showThemePreview(theme.id)
        }

        return view
    }
}
