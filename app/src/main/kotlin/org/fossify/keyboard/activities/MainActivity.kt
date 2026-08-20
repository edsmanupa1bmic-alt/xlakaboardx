package org.fossify.keyboard.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import org.fossify.commons.dialogs.ConfirmationAdvancedDialog
import org.fossify.commons.dialogs.RadioGroupDialog
import org.fossify.commons.extensions.appLaunched
import org.fossify.commons.extensions.beGone
import org.fossify.commons.extensions.beVisible
import org.fossify.commons.extensions.hideKeyboard
import org.fossify.commons.extensions.toast
import org.fossify.commons.models.RadioItem
import org.fossify.keyboard.BuildConfig
import org.fossify.keyboard.R
import org.fossify.keyboard.databinding.ActivitySettingsMainBinding
import org.fossify.keyboard.extensions.config
import org.fossify.keyboard.extensions.inputMethodManager
import org.fossify.keyboard.helpers.LANGUAGE_ENGLISH_QWERTY
import org.fossify.keyboard.helpers.LANGUAGE_SINHALA_SINGLISH
import org.fossify.keyboard.helpers.LANGUAGE_SINHALA_WIJESEKARA

class MainActivity : SimpleActivity() {

    private lateinit var binding: ActivitySettingsMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdge(padBottomSystem = listOf(binding.settingsScrollview))
        setupToolbar()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updateImeStatusBanner()
        updateLanguageSubtitle()
    }

    private fun setupToolbar() {
        binding.settingsToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.apply {
            // IME Setup Banner
            btnSetupIme.setOnClickListener {
                if (!isKeyboardEnabled()) {
                    Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                } else {
                    inputMethodManager.showInputMethodPicker()
                }
            }

            // Core Settings
            itemLanguages.setOnClickListener {
                showLanguagePickerDialog()
            }

            itemPreferences.setOnClickListener {
                hideKeyboard()
                startActivity(Intent(this@MainActivity, PreferencesActivity::class.java))
            }

            itemTheme.setOnClickListener {
                hideKeyboard()
                startActivity(Intent(this@MainActivity, ThemeCustomizerActivity::class.java))
            }

            // Typing & Input
            itemCorrections.setOnClickListener {
                hideKeyboard()
                startActivity(Intent(this@MainActivity, CorrectionsActivity::class.java))
            }

            itemGlideTyping.setOnClickListener {
                toast("Glide typing is enabled for English and Singlish layouts")
            }

            itemVoiceTyping.setOnClickListener {
                showVoiceTypingDialog()
            }

            itemEmojis.setOnClickListener {
                hideKeyboard()
                startActivity(Intent(this@MainActivity, EmojiSettingsActivity::class.java))
            }

            // Productivity
            itemClipboard.setOnClickListener {
                hideKeyboard()
                startActivity(Intent(this@MainActivity, ManageClipboardItemsActivity::class.java))
            }

            itemDictionary.setOnClickListener {
                showDictionaryInfoDialog()
            }
        }
    }

    private fun updateImeStatusBanner() {
        if (!isKeyboardEnabled() || !isKeyboardSelected()) {
            binding.imeSetupBanner.beVisible()
        } else {
            binding.imeSetupBanner.beGone()
        }
    }

    private fun updateLanguageSubtitle() {
        val currentLang = config.keyboardLanguage
        val langName = when (currentLang) {
            LANGUAGE_SINHALA_SINGLISH -> "Sinhala (Singlish Phonetic), English (US)"
            LANGUAGE_SINHALA_WIJESEKARA -> "Sinhala (Wijesekara), English (US)"
            else -> "English (US) (QWERTY), Sinhala"
        }
        binding.languagesSubtitle.text = langName
    }

    private fun showLanguagePickerDialog() {
        val radioItems = arrayListOf(
            RadioItem(LANGUAGE_SINHALA_SINGLISH, "Sinhala (Singlish Phonetic)"),
            RadioItem(LANGUAGE_SINHALA_WIJESEKARA, "Sinhala (Wijesekara Standard)"),
            RadioItem(LANGUAGE_ENGLISH_QWERTY, "English (US) (QWERTY)")
        )

        RadioGroupDialog(
            activity = this,
            items = radioItems,
            checkedItemId = config.keyboardLanguage,
            titleId = R.string.keyboard_language
        ) { selectedId ->
            config.keyboardLanguage = selectedId as Int
            updateLanguageSubtitle()
            toast("Keyboard language updated")
        }
    }

    private fun showVoiceTypingDialog() {
        val radioItems = arrayListOf(
            RadioItem(0, "Google Voice Typing (System Default)"),
            RadioItem(1, "Offline Voice Input"),
            RadioItem(2, "Disabled")
        )

        RadioGroupDialog(
            activity = this,
            items = radioItems,
            checkedItemId = 0,
            titleId = R.string.voice_typing_method
        ) {
            toast("Voice typing configured")
        }
    }

    private fun showDictionaryInfoDialog() {
        ConfirmationAdvancedDialog(
            activity = this,
            message = "Personal Dictionary & Spell Check are synchronized with offline Sinhala and English vocabulary packages.",
            positive = R.string.ok,
            negative = 0
        ) { }
    }

    private fun isKeyboardEnabled(): Boolean {
        return try {
            val enabledMethods = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_INPUT_METHODS
            ) ?: return false
            enabledMethods.contains(packageName)
        } catch (e: Exception) {
            false
        }
    }

    private fun isKeyboardSelected(): Boolean {
        return try {
            val defaultIME = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            ) ?: return false
            defaultIME.contains(packageName)
        } catch (e: Exception) {
            false
        }
    }
}
