package org.fossify.keyboard.helpers

import android.content.Context
import android.content.SharedPreferences

class RecentEmojiManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val listeners = mutableListOf<() -> Unit>()

    companion object {
        private const val PREFS_NAME = "lakmal_recent_emojis"
        private const val KEY_RECENT_EMOJIS = "recent_emojis_list"
        private const val MAX_RECENT_EMOJIS = 20

        val DEFAULT_EMOJIS = listOf(
            "🥺", "😔", "💔", "😇", "😎",
            "😁", "😽", "❤️", "🤤", "😘"
        )

        @Volatile
        private var instance: RecentEmojiManager? = null

        fun getInstance(context: Context): RecentEmojiManager {
            return instance ?: synchronized(this) {
                instance ?: RecentEmojiManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    /**
     * Retrieves the current list of recently used emojis, or returns the default list.
     */
    fun getRecentEmojis(): List<String> {
        val raw = prefs.getString(KEY_RECENT_EMOJIS, null)
        return if (!raw.isNullOrEmpty()) {
            raw.split("|").filter { it.isNotEmpty() }
        } else {
            DEFAULT_EMOJIS
        }
    }

    /**
     * Adds or promotes an emoji to Index 0, removes duplicates, caps at capacity, and persists.
     */
    fun addEmoji(emoji: String) {
        if (emoji.isBlank()) return
        val currentList = getRecentEmojis().toMutableList()
        currentList.remove(emoji)
        currentList.add(0, emoji)

        val updatedList = currentList.take(MAX_RECENT_EMOJIS)
        prefs.edit().putString(KEY_RECENT_EMOJIS, updatedList.joinToString("|")).apply()

        notifyListeners()
    }

    fun addListener(listener: () -> Unit) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        for (listener in listeners) {
            listener.invoke()
        }
    }
}
