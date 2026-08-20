package org.fossify.keyboard.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ThemeImageManager {

    fun saveBackgroundImage(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return false

            val themesDir = File(context.filesDir, "themes")
            if (!themesDir.exists()) {
                themesDir.mkdirs()
            }

            val bgFile = File(themesDir, "custom_bg.jpg")
            val outputStream = FileOutputStream(bgFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
