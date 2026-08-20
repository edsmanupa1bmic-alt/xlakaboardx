package org.fossify.keyboard.activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import org.fossify.keyboard.R
import org.fossify.keyboard.helpers.Config
import org.fossify.keyboard.helpers.ThemeImageManager

class ThemeEditorActivity : SimpleActivity() {

    private lateinit var config: Config

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val success = ThemeImageManager.saveBackgroundImage(this, uri)
            if (success) {
                config.bgImageEnabled = true
                findViewById<SwitchCompat>(R.id.switch_bg_image).isChecked = true
                Toast.makeText(this, "Background Image Saved", Toast.LENGTH_SHORT).show()
                // You could refresh the preview here
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_editor)
        
        config = Config.newInstance(this)

        val switchBgImage = findViewById<SwitchCompat>(R.id.switch_bg_image)
        val btnPickImage = findViewById<Button>(R.id.btn_pick_image)
        val sliderDim = findViewById<Slider>(R.id.slider_dim_opacity)
        val sliderBlur = findViewById<Slider>(R.id.slider_blur_radius)
        val sliderAlpha = findViewById<Slider>(R.id.slider_key_translucency)
        val inputSpacebar = findViewById<TextInputEditText>(R.id.input_spacebar_text)
        val btnApply = findViewById<Button>(R.id.btn_apply_theme)

        switchBgImage.isChecked = config.bgImageEnabled
        sliderDim.value = config.bgDimOpacity
        sliderBlur.value = config.bgBlurRadius
        sliderAlpha.value = config.keyTranslucencyAlpha.toFloat()
        inputSpacebar.setText(config.customSpacebarText)

        btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnApply.setOnClickListener {
            config.bgImageEnabled = switchBgImage.isChecked
            config.bgDimOpacity = sliderDim.value
            config.bgBlurRadius = sliderBlur.value
            config.keyTranslucencyAlpha = sliderAlpha.value.toInt()
            config.customSpacebarText = inputSpacebar.text.toString()

            Toast.makeText(this, "Theme Applied!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
