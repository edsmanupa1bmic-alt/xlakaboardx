package org.fossify.keyboard

import android.app.Application
import org.fossify.commons.extensions.checkUseEnglish
import org.fossify.keyboard.extensions.isDeviceInDirectBootMode
import org.fossify.keyboard.helpers.EmojiManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!isDeviceInDirectBootMode) {
            checkUseEnglish()
        }
        EmojiManager.init(this)
    }
}
