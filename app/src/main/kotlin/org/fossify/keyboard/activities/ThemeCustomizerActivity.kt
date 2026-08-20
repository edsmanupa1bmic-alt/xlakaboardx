package org.fossify.keyboard.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import org.fossify.keyboard.R
import org.fossify.keyboard.helpers.Config
import org.fossify.keyboard.helpers.ThemeImageManager

class ThemeCustomizerActivity : SimpleActivity() {

    private lateinit var config: Config

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val success = ThemeImageManager.saveBackgroundImage(this, uri)
            if (success) {
                // Also save it as active_wallpaper.png for FlashboardThemeEngine
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    val themesDir = java.io.File(filesDir, "themes")
                    if (!themesDir.exists()) themesDir.mkdirs()
                    val bgFile = java.io.File(themesDir, "active_wallpaper.png")
                    val outputStream = java.io.FileOutputStream(bgFile)
                    bitmap?.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    
                    config.bgImageEnabled = true
                    Toast.makeText(this, "Wallpaper loaded", Toast.LENGTH_SHORT).show()
                    
                    // Update preview
                    val previewBg = findViewById<ImageView>(R.id.keyboard_bg_image)
                    previewBg?.setImageBitmap(bitmap)
                    previewBg?.visibility = android.view.View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_editor) // Reuse layout
        
        config = Config.newInstance(this)

        val btnPickImage = findViewById<Button>(R.id.btn_pick_image)
        val sliderDim = findViewById<Slider>(R.id.slider_dim_opacity)
        val sliderAlpha = findViewById<Slider>(R.id.slider_key_translucency)
        val inputSpacebar = findViewById<TextInputEditText>(R.id.input_spacebar_text)
        val btnApply = findViewById<Button>(R.id.btn_apply_theme)

        sliderDim.value = config.bgDimOpacity.coerceIn(0f, 1f)
        sliderAlpha.value = config.keyTranslucencyAlpha.toFloat().coerceIn(0f, 255f)
        inputSpacebar.setText(config.customSpacebarText)

        sliderDim.addOnChangeListener { _, value, _ ->
            findViewById<android.view.View>(R.id.keyboard_bg_dim)?.alpha = value
        }

        btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnApply.setOnClickListener {
            config.bgDimOpacity = sliderDim.value
            config.keyTranslucencyAlpha = sliderAlpha.value.toInt()
            config.customSpacebarText = inputSpacebar.text.toString()

            // Broadcast intent to IME service
            val intent = Intent("org.fossify.keyboard.THEME_CHANGED")
            sendBroadcast(intent)
            
            Toast.makeText(this, "Theme Applied!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
