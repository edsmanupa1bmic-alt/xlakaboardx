package org.fossify.keyboard.activities

import android.os.Bundle
import org.fossify.commons.extensions.updateTextColors
import org.fossify.commons.extensions.viewBinding
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.keyboard.BuildConfig
import org.fossify.keyboard.databinding.ActivityAboutCleanBinding

class AboutActivity : SimpleActivity() {
    private val binding by viewBinding(ActivityAboutCleanBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            setupEdgeToEdge(padBottomSystem = listOf(aboutNestedScrollview))
            setupMaterialScrollListener(aboutNestedScrollview, aboutAppbar)
            aboutVersionName.text = "v${BuildConfig.VERSION_NAME}"
        }
    }

    override fun onResume() {
        super.onResume()
        setupTopAppBar(binding.aboutAppbar, NavigationIcon.Arrow)
        updateTextColors(binding.aboutNestedScrollview)
    }
}
