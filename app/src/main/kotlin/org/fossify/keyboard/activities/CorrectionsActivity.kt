package org.fossify.keyboard.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.fossify.keyboard.databinding.ActivityCorrectionsBinding
import org.fossify.keyboard.extensions.config

class CorrectionsActivity : SimpleActivity() {

    private lateinit var binding: ActivityCorrectionsBinding

    private val requestContactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            config.suggestContacts = true
            binding.switchSuggestContacts.isChecked = true
        } else {
            config.suggestContacts = false
            binding.switchSuggestContacts.isChecked = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCorrectionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadPreferences()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.correctionsToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadPreferences() {
        binding.apply {
            // Automatic corrections
            switchAutoCorrection.isChecked = config.smartAutoCorrection
            switchAutoCapitalization.isChecked = config.enableSentencesCapitalization

            // Spelling & grammar
            switchSpellCheck.isChecked = config.spellCheckEnabled
            switchGrammarCheck.isChecked = config.grammarCheckEnabled

            // Suggestions
            switchBlockOffensive.isChecked = config.blockOffensiveWords
            switchWordSuggestions.isChecked = config.suggestionStripEnabled
            switchNextWordSuggestions.isChecked = config.nextWordSuggestions
            switchSuggestContacts.isChecked = config.suggestContacts
        }
    }

    private fun setupListeners() {
        binding.apply {
            switchAutoCorrection.setOnCheckedChangeListener { _, isChecked ->
                config.smartAutoCorrection = isChecked
            }

            switchAutoCapitalization.setOnCheckedChangeListener { _, isChecked ->
                config.enableSentencesCapitalization = isChecked
            }

            switchSpellCheck.setOnCheckedChangeListener { _, isChecked ->
                config.spellCheckEnabled = isChecked
            }

            switchGrammarCheck.setOnCheckedChangeListener { _, isChecked ->
                config.grammarCheckEnabled = isChecked
            }

            switchBlockOffensive.setOnCheckedChangeListener { _, isChecked ->
                config.blockOffensiveWords = isChecked
            }

            switchWordSuggestions.setOnCheckedChangeListener { _, isChecked ->
                config.suggestionStripEnabled = isChecked
            }

            switchNextWordSuggestions.setOnCheckedChangeListener { _, isChecked ->
                config.nextWordSuggestions = isChecked
            }

            switchSuggestContacts.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (ContextCompat.checkSelfPermission(this@CorrectionsActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    } else {
                        config.suggestContacts = true
                    }
                } else {
                    config.suggestContacts = false
                }
            }
        }
    }
}
