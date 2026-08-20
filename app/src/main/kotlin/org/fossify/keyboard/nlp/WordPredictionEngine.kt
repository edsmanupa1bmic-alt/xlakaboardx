package org.fossify.keyboard.nlp

import android.content.Context
import android.content.SharedPreferences
import org.fossify.keyboard.helpers.SinglishParser
import java.util.PriorityQueue

data class SuggestionItem(
    val text: String,
    val isSinhala: Boolean,
    val isPrimary: Boolean = false,
    val isExactMatch: Boolean = false
)

class WordPredictionEngine private constructor() {

    private class TrieNode(val char: Char) {
        val children = HashMap<Char, TrieNode>()
        var isWord: Boolean = false
        var frequency: Int = 0
        var word: String? = null
    }

    private val sinhalaTrie = TrieNode(' ')
    private val englishTrie = TrieNode(' ')
    private val userLearnedSinhala = HashMap<String, Int>()
    private val userLearnedEnglish = HashMap<String, Int>()
    private var prefs: SharedPreferences? = null

    private val englishTypos = mapOf(
        "teh" to "the", "adn" to "and", "waht" to "what", "taht" to "that",
        "wiht" to "with", "tiem" to "time", "recieve" to "receive", "recieved" to "received",
        "seperate" to "separate", "definately" to "definitely", "untill" to "until",
        "becuase" to "because", "beleive" to "believe", "thier" to "their", "wierd" to "weird",
        "dont" to "don't", "cant" to "can't", "wont" to "won't", "didnt" to "didn't",
        "isnt" to "isn't", "arent" to "aren't", "couldnt" to "couldn't", "wouldnt" to "wouldn't",
        "shouldnt" to "shouldn't", "hasnt" to "hasn't", "havent" to "haven't",
        "im" to "I'm", "youre" to "you're", "theyre" to "they're", "ill" to "I'll", "ive" to "I've",
        "hellp" to "hello", "thx" to "thanks", "ty" to "thank you", "pls" to "please", "plz" to "please",
        "sory" to "sorry", "tommorow" to "tomorrow", "tomorow" to "tomorrow", "alot" to "a lot",
        "gud" to "good", "bt" to "but", "ur" to "your", "hw" to "how",
        "thnks" to "thanks", "welcom" to "welcome", "bday" to "birthday",
        "rember" to "remember", "truely" to "truly", "freind" to "friend", "freinds" to "friends",
        "peaple" to "people", "intresting" to "interesting", "fav" to "favorite"
    )

    private val sinhalaTypos = mapOf(
        "කරනන" to "කරන්න", "යනන" to "යන්න", "ඉනන" to "ඉන්න", "ගනන" to "ගන්න", "දෙනන" to "දෙන්න",
        "බලනන" to "බලන්න", "කියනන" to "කියන්න", "හිතනන" to "හිතන්න", "එනන" to "එන්න",
        "සතුටුඉ" to "සතුටුයි", "හොඳඉ" to "හොඳයි", "ලස්සනඉ" to "ලස්සනයි", "එලඉ" to "එළයි",
        "ආදරෙඉ" to "ආදරෙයි", "නැහෑ" to "නැහැ", "නෑහෑ" to "නැහැ", "නෙවෙඉ" to "නෙවෙයි",
        "ස්තුති" to "ස්තූතියි", "ස්තුතියි" to "ස්තූතියි", "ආයුබෝවන" to "ආයුබෝවන්",
        "සුබ පැතුම්" to "සුබපැතුම්", "පරිස්සමින්" to "පරිස්සමෙන්", "එල" to "එළ"
    )

    private val singlishInputCorrections = mapOf(
        "thx" to "ස්තූතියි",
        "ty" to "ස්තූතියි",
        "sthuthi" to "ස්තූතියි",
        "sthuthiy" to "ස්තූතියි",
        "stuti" to "ස්තූතියි",
        "ayubowan" to "ආයුබෝවන්",
        "ayubowang" to "ආයුබෝවන්",
        "aayubowan" to "ආයුබෝවන්",
        "subapathum" to "සුබපැතුම්",
        "puluwan" to "පුළුවන්",
        "kohomada" to "කොහොමද",
        "mokada" to "මොකද",
        "mokakda" to "මොකක්ද",
        "machang" to "මචං",
        "machan" to "මචන්"
    )

    companion object {
        private const val PREFS_NAME = "lakmal_user_dictionary"
        private const val KEY_SINHALA_WORDS = "learned_sinhala_words"
        private const val KEY_ENGLISH_WORDS = "learned_english_words"

        @Volatile
        private var instance: WordPredictionEngine? = null

        fun getInstance(): WordPredictionEngine {
            return instance ?: synchronized(this) {
                instance ?: WordPredictionEngine().also {
                    it.initDictionaries()
                    instance = it
                }
            }
        }
    }

    fun initPreferences(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadUserLearnedWords()
        }
    }

    private fun insertWord(root: TrieNode, word: String, freq: Int) {
        if (word.isBlank()) return
        var current = root
        for (char in word) {
            current = current.children.getOrPut(char) { TrieNode(char) }
        }
        current.isWord = true
        current.word = word
        current.frequency = maxOf(current.frequency, freq)
    }

    private fun initDictionaries() {
        // 1. High-frequency Sinhala Vocabulary
        val commonSinhala = listOf(
            // Pronouns & Core Questions
            "මම" to 500, "මට" to 480, "මගේ" to 470, "අපි" to 460, "අපිට" to 450, "අපේ" to 440,
            "ඔයා" to 520, "ඔයාට" to 510, "ඔයාගේ" to 500, "ඔයාලා" to 470, "ඔයාලට" to 460, "ඔයාලගේ" to 450,
            "එයා" to 420, "එයාට" to 410, "එයාගේ" to 400, "එයාලා" to 390,
            "මොකද" to 460, "මොකක්ද" to 450, "කොහොමද" to 480, "කොහෙද" to 440, "ඇයි" to 450,
            "කවුද" to 430, "කවදාද" to 400, "කීයද" to 380, "කොච්චරද" to 370, "මොනවද" to 410,

            // Common Verbs & States
            "කරන්න" to 490, "කරනවා" to 480, "කරන්නේ" to 470, "කළා" to 440, "කරලා" to 450, "කරමු" to 430,
            "වෙනවා" to 460, "වුණා" to 440, "වෙලා" to 450, "වෙන්න" to 455,
            "ගන්න" to 470, "ගත්තා" to 440, "ගන්නේ" to 430, "ගන්නවා" to 450,
            "එන්න" to 460, "ආවා" to 450, "එනවා" to 440, "එමු" to 410,
            "යන්න" to 470, "ගියා" to 460, "යනවා" to 450, "යමු" to 440,
            "ඉන්න" to 480, "හිටියා" to 450, "ඉන්නවා" to 470, "ඉන්නේ" to 460,
            "තියෙනවා" to 490, "තිබ්බා" to 450, "තියෙන්නේ" to 460, "තියෙයි" to 410,
            "බලන්න" to 440, "බැලුවා" to 420, "බලනවා" to 430,
            "කියන්න" to 460, "කිව්වා" to 450, "කියනවා" to 440,
            "දෙන්න" to 450, "දුන්නා" to 430, "දෙනවා" to 420,
            "හිතන්න" to 420, "හිතුණා" to 410, "හිතනවා" to 400,
            "දන්නවා" to 460, "දන්නේ" to 440, "දන්නෑ" to 410,

            // Negation & Modals
            "නෑ" to 500, "නැහැ" to 490, "නේද" to 480, "නෙවෙයි" to 460,
            "පුළුවන්" to 480, "බෑ" to 470, "බැහැ" to 460, "ඕන" to 470, "ඕනේ" to 480, "එපා" to 460,

            // Polite phrases & Greetings
            "ස්තූතියි" to 490, "ආයුබෝවන්" to 480, "සුබ" to 460, "පැතුම්" to 440, "සුබපැතුම්" to 470,
            "සුබ උදෑසනක්" to 450, "සුබ රාත්‍රියක්" to 440,
            "හොඳයි" to 480, "හරි" to 490, "ලස්සනයි" to 440, "එල" to 460, "සුපිරි" to 470,
            "ආදරෙයි" to 460, "පරිස්සමෙන්" to 450, "සතුටුයි" to 430, "කමක් නෑ" to 440,

            // Common Nouns & Adverbs
            "දැන්" to 480, "පස්සේ" to 470, "අද" to 480, "ඊයේ" to 440, "හෙට" to 460,
            "ඉක්මනින්" to 430, "ටිකක්" to 460, "ගොඩක්" to 480, "වගේ" to 470, "ගැන" to 460,
            "එක්ක" to 470, "සමඟ" to 430, "නිසා" to 460, "හින්දා" to 470, "විතරක්" to 440,
            "නමුත්" to 450, "සහ" to 460, "නැත්නම්" to 440,
            "ගෙදර" to 470, "රට" to 440, "වැඩ" to 470, "යාළුවා" to 460, "මිතුරා" to 420,
            "පොත" to 410, "පාසල" to 420, "කාර්යාලය" to 400, "දුරකථනය" to 430, "පණිවිඩය" to 420,
            "මුදල්" to 440, "වෙලාව" to 460, "තැන" to 430, "ලංකාව" to 460, "සිංහල" to 470,
            "යාලුවා" to 450, "මචං" to 480, "මචන්" to 480, "බ්‍රෝ" to 460
        )

        for ((word, freq) in commonSinhala) {
            insertWord(sinhalaTrie, word, freq)
        }

        // 2. High-frequency English Vocabulary
        val commonEnglish = listOf(
            "the" to 500, "be" to 490, "to" to 490, "of" to 480, "and" to 480, "a" to 480,
            "in" to 470, "that" to 470, "have" to 460, "i" to 500, "it" to 470, "for" to 460,
            "not" to 460, "on" to 450, "with" to 450, "he" to 440, "as" to 440, "you" to 500,
            "do" to 470, "at" to 450, "this" to 470, "but" to 460, "his" to 430, "by" to 430,
            "from" to 440, "they" to 450, "we" to 470, "say" to 440, "her" to 420, "she" to 440,
            "or" to 450, "an" to 430, "will" to 470, "my" to 480, "one" to 450, "all" to 460,
            "would" to 440, "there" to 450, "their" to 440, "what" to 470, "so" to 460, "up" to 450,
            "out" to 440, "if" to 450, "about" to 450, "who" to 440, "get" to 460, "which" to 430,
            "go" to 470, "me" to 480, "when" to 460, "make" to 450, "can" to 480, "like" to 470,
            "time" to 460, "no" to 470, "just" to 460, "him" to 430, "know" to 470, "take" to 450,
            "people" to 440, "into" to 430, "year" to 420, "your" to 480, "good" to 480, "some" to 450,
            "could" to 440, "them" to 440, "see" to 460, "other" to 430, "than" to 430, "then" to 450,
            "now" to 470, "look" to 450, "only" to 440, "come" to 460, "its" to 430, "over" to 430,
            "think" to 460, "also" to 450, "back" to 450, "after" to 440, "use" to 440, "two" to 430,
            "how" to 470, "our" to 460, "work" to 460, "first" to 440, "well" to 450, "way" to 450,
            "even" to 430, "new" to 450, "want" to 460, "because" to 450, "any" to 440, "these" to 430,
            "give" to 450, "day" to 450, "most" to 440, "us" to 460,
            "hello" to 490, "hi" to 490, "thanks" to 490, "thank" to 480, "okay" to 490, "ok" to 490,
            "yes" to 490, "please" to 480, "sorry" to 480, "love" to 480, "friend" to 470, "happy" to 470,
            "great" to 470, "nice" to 470, "cool" to 470, "bro" to 480, "machan" to 480, "super" to 470
        )

        for ((word, freq) in commonEnglish) {
            insertWord(englishTrie, word, freq)
        }
    }

    private fun loadUserLearnedWords() {
        prefs?.let { sp ->
            val sinhalaData = sp.getString(KEY_SINHALA_WORDS, "") ?: ""
            if (sinhalaData.isNotEmpty()) {
                sinhalaData.split(";").forEach { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 2) {
                        val word = parts[0]
                        val freq = parts[1].toIntOrNull() ?: 600
                        userLearnedSinhala[word] = freq
                        insertWord(sinhalaTrie, word, freq)
                    }
                }
            }

            val englishData = sp.getString(KEY_ENGLISH_WORDS, "") ?: ""
            if (englishData.isNotEmpty()) {
                englishData.split(";").forEach { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 2) {
                        val word = parts[0]
                        val freq = parts[1].toIntOrNull() ?: 600
                        userLearnedEnglish[word] = freq
                        insertWord(englishTrie, word, freq)
                    }
                }
            }
        }
    }

    /**
     * Dynamically learns a new committed word or increments its frequency.
     */
    fun learnWord(word: String, isSinhala: Boolean) {
        val trimmed = word.trim()
        if (trimmed.length < 2) return

        val targetMap = if (isSinhala) userLearnedSinhala else userLearnedEnglish
        val currentFreq = targetMap[trimmed] ?: 550
        val updatedFreq = currentFreq + 25
        targetMap[trimmed] = updatedFreq

        val targetTrie = if (isSinhala) sinhalaTrie else englishTrie
        insertWord(targetTrie, trimmed, updatedFreq)

        saveUserLearnedWords()
    }

    private fun saveUserLearnedWords() {
        prefs?.edit()?.apply {
            val sinhalaData = userLearnedSinhala.entries.joinToString(";") { "${it.key}:${it.value}" }
            val englishData = userLearnedEnglish.entries.joinToString(";") { "${it.key}:${it.value}" }
            putString(KEY_SINHALA_WORDS, sinhalaData)
            putString(KEY_ENGLISH_WORDS, englishData)
            apply()
        }
    }

    /**
     * Generates prioritized Dual-Language suggestions for the typed raw buffer.
     */
    fun getSuggestions(buffer: String, isSinglishMode: Boolean = true): List<SuggestionItem> {
        if (buffer.isBlank()) return emptyList()

        val results = ArrayList<SuggestionItem>()
        val seenWords = HashSet<String>()

        if (isSinglishMode) {
            // 1. Candidate 1 (Exact Transliteration from SinglishParser)
            val sinhalaExact = SinglishParser.parse(buffer)
            if (sinhalaExact.isNotBlank()) {
                results.add(
                    SuggestionItem(
                        text = sinhalaExact,
                        isSinhala = true,
                        isPrimary = true,
                        isExactMatch = true
                    )
                )
                seenWords.add(sinhalaExact)
            }

            // 2. Candidates 2-4+: Search Sinhala Trie completions with root matching sinhalaExact
            if (sinhalaExact.isNotEmpty()) {
                val completions = searchTrie(sinhalaTrie, sinhalaExact, limit = 5)
                for ((word, _) in completions) {
                    if (word !in seenWords) {
                        results.add(
                            SuggestionItem(
                                text = word,
                                isSinhala = true,
                                isPrimary = false,
                                isExactMatch = false
                            )
                        )
                        seenWords.add(word)
                    }
                }
            }

            // 3. English alternatives / completions for the typed latin prefix
            val lowerBuffer = buffer.lowercase()
            val englishMatches = searchTrie(englishTrie, lowerBuffer, limit = 3)
            for ((engWord, _) in englishMatches) {
                if (engWord !in seenWords) {
                    results.add(
                        SuggestionItem(
                            text = engWord,
                            isSinhala = false,
                            isPrimary = false,
                            isExactMatch = false
                        )
                    )
                    seenWords.add(engWord)
                }
            }

            // Also offer raw Latin buffer if not present
            if (buffer !in seenWords && buffer.length > 1) {
                results.add(
                    SuggestionItem(
                        text = buffer,
                        isSinhala = false,
                        isPrimary = false,
                        isExactMatch = false
                    )
                )
                seenWords.add(buffer)
            }
        } else {
            // English / Latin Mode
            val lowerBuffer = buffer.lowercase()
            val englishMatches = searchTrie(englishTrie, lowerBuffer, limit = 8)
            var isFirst = true
            for ((engWord, _) in englishMatches) {
                val formatted = if (buffer.firstOrNull()?.isUpperCase() == true) {
                    engWord.replaceFirstChar { it.uppercase() }
                } else {
                    engWord
                }
                if (formatted !in seenWords) {
                    results.add(
                        SuggestionItem(
                            text = formatted,
                            isSinhala = false,
                            isPrimary = isFirst,
                            isExactMatch = (formatted.equals(buffer, ignoreCase = true))
                        )
                    )
                    seenWords.add(formatted)
                    isFirst = false
                }
            }

            if (buffer !in seenWords) {
                results.add(0,
                    SuggestionItem(
                        text = buffer,
                        isSinhala = false,
                        isPrimary = results.isEmpty(),
                        isExactMatch = true
                    )
                )
            }
        }

        return results.take(10)
    }

    private fun searchTrie(root: TrieNode, prefix: String, limit: Int): List<Pair<String, Int>> {
        var current = root
        for (char in prefix) {
            current = current.children[char] ?: return emptyList()
        }

        val pq = PriorityQueue<Pair<String, Int>>(compareByDescending { it.second })
        collectWords(current, pq, maxCollect = limit * 4, depth = 0, maxDepth = 8)

        val results = ArrayList<Pair<String, Int>>()
        while (pq.isNotEmpty() && results.size < limit) {
            results.add(pq.poll()!!)
        }
        return results
    }

    private fun collectWords(
        node: TrieNode,
        pq: PriorityQueue<Pair<String, Int>>,
        maxCollect: Int,
        depth: Int,
        maxDepth: Int
    ) {
        if (depth > maxDepth || pq.size >= maxCollect) return

        if (node.isWord && node.word != null) {
            pq.add(Pair(node.word!!, node.frequency))
        }
        for (child in node.children.values) {
            if (pq.size >= maxCollect) break
            collectWords(child, pq, maxCollect, depth + 1, maxDepth)
        }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    /**
     * Checks if the given buffer / word has a known typo or high-confidence auto-correction.
     * Returns the corrected word, or null if no correction is applicable.
     */
    fun getAutoCorrection(rawWord: String, isSinglish: Boolean): String? {
        val trimmed = rawWord.trim()
        if (trimmed.length < 2) return null

        if (isSinglish) {
            val lower = trimmed.lowercase()
            // 1. Direct Singlish keyword typo / shortcut fix
            singlishInputCorrections[lower]?.let { return it }

            // 2. Direct match on phonetic typo for converted Sinhala text
            val parsedSinhala = SinglishParser.parse(trimmed)
            sinhalaTypos[parsedSinhala]?.let { return it }
            sinhalaTypos[trimmed]?.let { return it }

            return null
        } else {
            val lower = trimmed.lowercase()
            // 1. Direct English typo dictionary check
            englishTypos[lower]?.let { replacement ->
                return if (trimmed.firstOrNull()?.isUpperCase() == true) {
                    replacement.replaceFirstChar { it.uppercase() }
                } else {
                    replacement
                }
            }

            // 2. If word is already a valid word in Trie, do not change it
            val exactMatches = searchTrie(englishTrie, lower, limit = 1)
            if (exactMatches.isNotEmpty() && exactMatches.first().first.equals(lower, ignoreCase = true)) {
                return null
            }

            // 3. Fuzzy distance matching for typos >= 4 characters
            if (lower.length >= 4) {
                val prefix = lower.substring(0, 2)
                val candidates = searchTrie(englishTrie, prefix, limit = 10)
                for ((candidate, freq) in candidates) {
                    if (freq >= 450 && levenshteinDistance(lower, candidate) == 1) {
                        return if (trimmed.firstOrNull()?.isUpperCase() == true) {
                            candidate.replaceFirstChar { it.uppercase() }
                        } else {
                            candidate
                        }
                    }
                }
            }

            return null
        }
    }
}
