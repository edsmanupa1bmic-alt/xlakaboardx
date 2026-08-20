package org.fossify.keyboard.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.fossify.commons.extensions.toast
import org.fossify.keyboard.R
import org.fossify.keyboard.databinding.FragmentThemePreviewBinding
import org.fossify.keyboard.helpers.ThemeEngine
import org.fossify.keyboard.models.ParsedFlorisTheme

class ThemePreviewFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentThemePreviewBinding? = null
    private val binding get() = _binding!!

    private var themeId: String = ""
    private var onThemeAppliedCallback: (() -> Unit)? = null

    companion object {
        private const val ARG_THEME_ID = "arg_theme_id"

        fun newInstance(themeId: String, onApplied: (() -> Unit)? = null): ThemePreviewFragment {
            val fragment = ThemePreviewFragment()
            val args = Bundle()
            args.putString(ARG_THEME_ID, themeId)
            fragment.arguments = args
            fragment.onThemeAppliedCallback = onApplied
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeId = arguments?.getString(ARG_THEME_ID) ?: ThemeEngine.THEME_DEFAULT_DARK
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val theme = ThemeEngine.getAllThemes(context).find { it.id == themeId }
            ?: ThemeEngine.getActiveTheme(context)

        bindTheme(theme)
    }

    private fun bindTheme(theme: ParsedFlorisTheme) {
        val ctx = requireContext()

        // 1. Header Information
        binding.previewDialogTitle.text = theme.name
        val nightLabel = if (theme.isNight) "Dark Mode" else "Light Mode"
        val authorLabel = if (theme.author.isNotBlank()) "By ${theme.author}" else "FlorisBoard Parsed Theme"
        binding.previewDialogAuthor.text = "$authorLabel • $nightLabel"

        // 2. Mockup Keyboard Container Background
        binding.mockupKeyboardContainer.background = ThemeEngine.getKeyboardBackground(ctx, theme)

        // 3. Mockup Smartbar Toolbar & Chips
        binding.mockupSmartbar.background = ThemeEngine.getSmartbarBackground(ctx, theme)

        binding.mockupChip1.apply {
            background = ThemeEngine.getCandidateChipBackground(ctx, isPrimary = true, theme)
            setTextColor(theme.accentKeyTextColor)
        }

        binding.mockupChip2.apply {
            background = ThemeEngine.getCandidateChipBackground(ctx, isPrimary = false, theme)
            setTextColor(theme.keyTextColor)
        }

        binding.mockupChip3.apply {
            background = ThemeEngine.getCandidateChipBackground(ctx, isPrimary = false, theme)
            setTextColor(theme.keyTextColor)
        }

        // 4. Style All Mock Keys
        val standardKeys = listOf(
            binding.mockKeyQ, binding.mockKeyW, binding.mockKeyE, binding.mockKeyR, binding.mockKeyT,
            binding.mockKeyY, binding.mockKeyU, binding.mockKeyI, binding.mockKeyO, binding.mockKeyP,
            binding.mockKeyA, binding.mockKeyS, binding.mockKeyD, binding.mockKeyF, binding.mockKeyG,
            binding.mockKeyH, binding.mockKeyJ, binding.mockKeyK, binding.mockKeyL,
            binding.mockKeyShift, binding.mockKeyZ, binding.mockKeyX, binding.mockKeyC, binding.mockKeyV,
            binding.mockKeyB, binding.mockKeyN, binding.mockKeyM, binding.mockKeyDelete,
            binding.mockKeySymbols, binding.mockKeyEmoji, binding.mockKeySpace
        )

        val keyDrawable = ThemeEngine.getKeyDrawable(ctx, isAction = false, theme)
        for (key in standardKeys) {
            key.background = keyDrawable.constantState?.newDrawable()?.mutate() ?: ThemeEngine.getKeyDrawable(ctx, isAction = false, theme)
            key.setTextColor(theme.keyTextColor)
        }

        // 5. Action Enter Key
        binding.mockKeyEnter.apply {
            background = ThemeEngine.getKeyDrawable(ctx, isAction = true, theme)
            setTextColor(theme.accentKeyTextColor)
        }

        // 6. Color Palette Swatches
        fun setSwatch(v: View, color: Int) {
            val shape = GradientDrawable().apply {
                this.shape = GradientDrawable.OVAL
                setColor(color)
                setStroke(2, Color.parseColor("#33FFFFFF"))
            }
            v.background = shape
        }

        setSwatch(binding.paletteBg, theme.keyboardBgColor)
        setSwatch(binding.paletteKey, theme.keyBgColor)
        setSwatch(binding.paletteAccent, theme.accentKeyBgColor)
        setSwatch(binding.paletteText, theme.keyTextColor)

        // 7. Buttons Actions
        binding.previewDialogClose.setOnClickListener {
            dismiss()
        }

        binding.previewBtnCancel.setOnClickListener {
            dismiss()
        }

        binding.previewBtnApply.setOnClickListener {
            ThemeEngine.setActiveTheme(ctx, theme.id)
            ctx.toast(R.string.theme_applied)
            onThemeAppliedCallback?.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
