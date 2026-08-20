package org.fossify.keyboard.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.fossify.keyboard.R
import org.fossify.keyboard.activities.MainActivity

/**
 * SetupActivity: Dedicated, elegant Material 3 onboarding and launcher activity for xLakaBoardx.
 * Guides the user through:
 * 1. Enabling the Keyboard in Android System Settings.
 * 2. Selecting xLakaBoardx as the active Input Method.
 * 3. Testing the keyboard live in an integrated test field.
 * 4. Navigating to the Full Settings Hub (MainActivity).
 */
class SetupActivity : AppCompatActivity() {

    private lateinit var step1Container: LinearLayout
    private lateinit var step2Container: LinearLayout
    private lateinit var step3Container: LinearLayout

    private lateinit var step1Icon: ImageView
    private lateinit var step2Icon: ImageView
    private lateinit var step3Icon: ImageView

    private lateinit var btnEnableKeyboard: Button
    private lateinit var btnSelectKeyboard: Button
    private lateinit var btnOpenSettings: Button
    private lateinit var btnFinishSetup: Button

    private lateinit var tvStatusMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        initViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updateSetupState()
    }

    private fun initViews() {
        step1Container = findViewById(R.id.step1_container)
        step2Container = findViewById(R.id.step2_container)
        step3Container = findViewById(R.id.step3_container)

        step1Icon = findViewById(R.id.step1_status_icon)
        step2Icon = findViewById(R.id.step2_status_icon)
        step3Icon = findViewById(R.id.step3_status_icon)

        btnEnableKeyboard = findViewById(R.id.btn_enable_keyboard)
        btnSelectKeyboard = findViewById(R.id.btn_select_keyboard)
        btnOpenSettings = findViewById(R.id.btn_open_settings)
        btnFinishSetup = findViewById(R.id.btn_finish_setup)

        tvStatusMessage = findViewById(R.id.tv_status_message)
    }

    private fun setupListeners() {
        btnEnableKeyboard.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        btnSelectKeyboard.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        }

        btnOpenSettings.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnFinishSetup.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun updateSetupState() {
        val isEnabled = isKeyboardEnabled()
        val isDefault = isKeyboardDefault()

        if (isEnabled) {
            step1Icon.setImageResource(R.drawable.ic_check_vector)
            step1Icon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_light))
            btnEnableKeyboard.isEnabled = false
            btnEnableKeyboard.text = getString(R.string.step_completed_enabled)
        } else {
            step1Icon.setImageResource(R.drawable.ic_settings_cog_vector)
            step1Icon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_blue_light))
            btnEnableKeyboard.isEnabled = true
            btnEnableKeyboard.text = getString(R.string.step_enable_keyboard)
        }

        if (isDefault) {
            step2Icon.setImageResource(R.drawable.ic_check_vector)
            step2Icon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_light))
            btnSelectKeyboard.isEnabled = false
            btnSelectKeyboard.text = getString(R.string.step_completed_selected)
        } else {
            step2Icon.setImageResource(R.drawable.ic_keyboard_vector)
            step2Icon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_blue_light))
            btnSelectKeyboard.isEnabled = isEnabled
            btnSelectKeyboard.text = getString(R.string.step_select_keyboard)
        }

        if (isEnabled && isDefault) {
            tvStatusMessage.text = getString(R.string.setup_all_done)
            step3Container.visibility = View.VISIBLE
            btnFinishSetup.visibility = View.VISIBLE
        } else if (isEnabled) {
            tvStatusMessage.text = getString(R.string.setup_please_select_default)
            step3Container.visibility = View.GONE
            btnFinishSetup.visibility = View.GONE
        } else {
            tvStatusMessage.text = getString(R.string.setup_please_enable)
            step3Container.visibility = View.GONE
            btnFinishSetup.visibility = View.GONE
        }
    }

    private fun isKeyboardEnabled(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
        val enabledMethods = imm.enabledInputMethodList
        val currentPackage = packageName
        return enabledMethods.any { it.packageName == currentPackage }
    }

    private fun isKeyboardDefault(): Boolean {
        val defaultIme = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: return false
        return defaultIme.contains(packageName)
    }
}
