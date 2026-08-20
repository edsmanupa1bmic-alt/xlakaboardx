package org.fossify.keyboard.helpers

object SinglishParser {

    private const val HAL = "\u0DCA"
    private const val ZWJ = "\u200D"

    // Vowel modifier rules when following a consonant (longest match first)
    private val vowelModifierRules = listOf(
        "ruu" to "\u0DF2", // ෲ (kruu -> කෲ)
        "ru"  to "\u0DD8", // ෘ (kru -> කෘ)
        "aAa" to "\u0DD1", // ෑ
        "aAA" to "\u0DD1", // ෑ
        "Aa"  to "\u0DD1", // ෑ (kAa -> කෑ)
        "AA"  to "\u0DD1", // ෑ (kAA -> කෑ)
        "A"   to "\u0DD0", // ැ (kA -> කැ)
        "aa"  to "\u0DCF", // ා (kaa -> කා)
        "a"   to "",       // Inherent pure consonant (ka -> ක)
        "ii"  to "\u0DD3", // ී (kii -> කී)
        "i"   to "\u0DD2", // ි (ki -> කි)
        "uu"  to "\u0DD6", // ූ (kuu -> කූ)
        "u"   to "\u0DD4", // ු (ku -> කු, Lu -> ළු)
        "ee"  to "\u0DDA", // ේ (kee -> කේ)
        "e"   to "\u0DD9", // ෙ (ke -> කෙ)
        "ai"  to "\u0DDB", // ෛ (kai -> කෛ)
        "oo"  to "\u0DDD", // ෝ (koo -> කෝ)
        "o"   to "\u0DDC", // ො (ko -> කො)
        "au"  to "\u0DDE", // ෞ (kau -> කෞ)
        "ou"  to "\u0DDE", // ෞ (kou -> කෞ)
        "aH"  to "\u0D83", // ඃ (kaH -> කඃ)
        "H"   to "\u0D83", // ඃ
        "ax"  to "\u0D82", // ං (kax -> කං)
        "azn" to "\u0D82", // ං (kazn -> කං)
        "x"   to "\u0D82", // ං
        "aX"  to "\u0D9E", // ඞ (kaX -> කඞ)
        "X"   to "\u0D9E"  // ඞ
    )

    // Independent vowels (start of word / standalone / following another vowel)
    private val independentVowelRules = listOf(
        "Ru"  to "\u0D8E", // ඎ
        "R"   to "\u0D8D", // ඍ
        "Aa"  to "\u0D88", // ඈ
        "AA"  to "\u0D88", // ඈ
        "A"   to "\u0D87", // ඇ
        "aa"  to "\u0D86", // ආ
        "a"   to "\u0D85", // අ
        "ii"  to "\u0D8A", // ඊ
        "i"   to "\u0D89", // ඉ
        "uu"  to "\u0D8C", // ඌ
        "u"   to "\u0D8B", // උ
        "ee"  to "\u0D92", // ඒ
        "e"   to "\u0D91", // එ
        "ai"  to "\u0D93", // ඓ
        "oo"  to "\u0D95", // ඕ
        "o"   to "\u0D94", // ඔ
        "au"  to "\u0D96", // ඖ
        "ou"  to "\u0D96"  // ඖ
    )

    // Consonant stems (sorted longest pattern first)
    private val consonantRules = listOf(
        // Sanyaka with z prefix (3 letters)
        "zdh" to "\u0DB3", // ඳ (zdha -> සඳ/ඳ)

        // Mahaprana (3 letters)
        "chh" to "\u0DA1", // ඡ (chha -> ඡ)
        "thh" to "\u0DAE", // ථ (thha -> ථ)
        "dhh" to "\u0DB0", // ධ (dhha -> ධ)

        // Sanyaka with z prefix (2 letters)
        "zg"  to "\u0D9F", // ඟ (zga -> ඟ)
        "zj"  to "\u0DA6", // ඦ (zja -> ඦ)
        "zd"  to "\u0DAC", // ඬ (zda -> ඬ)
        "zq"  to "\u0DB3", // ඳ (zqa -> ඳ)
        "zk"  to "\u0DA4", // ඤ (zka -> ඤ)
        "zh"  to "\u0DA5", // ඥ (zha -> ඥ)

        // Mahaprana & Murdhaja (2 letters)
        "kh"  to "\u0D9B", // ඛ (kha -> ඛ)
        "gh"  to "\u0D9D", // ඝ (gha -> ඝ)
        "ph"  to "\u0DB5", // ඵ (pha -> ඵ)
        "bh"  to "\u0DB7", // භ (bha -> භ)
        "Sh"  to "\u0DC2", // ෂ (Sha -> ෂ)

        // Standard consonants (2 letters)
        "ch"  to "\u0DA0", // ච (cha -> ච)
        "th"  to "\u0DAD", // ත (tha -> ත)
        "dh"  to "\u0DAF", // ද (dha -> ද)
        "sh"  to "\u0DC1", // ශ (sha -> ශ)

        // Single letter Murdhaja / Special
        "q"   to "\u0DAF", // ද (qa -> ද)
        "T"   to "\u0DA8", // ඨ (Ta -> ඨ)
        "D"   to "\u0DAA", // ඪ (Da -> ඪ)
        "N"   to "\u0DAB", // ණ (Na -> ණ)
        "L"   to "\u0DC5", // ළ (La -> ළ, Lu -> ළු)
        "S"   to "\u0DC2", // ෂ (Sa -> ෂ)
        "B"   to "\u0DB9", // ඹ (Ba -> ඹ)

        // Standard single letter consonants
        "k"   to "\u0D9A", // ක (ka -> ක, k -> ක්)
        "g"   to "\u0D9C", // ග (ga -> ග)
        "j"   to "\u0DA2", // ජ (ja -> ජ)
        "t"   to "\u0DA7", // ට (ta -> ට)
        "d"   to "\u0DA9", // ඩ (da -> ඩ)
        "n"   to "\u0DB1", // න (na -> න)
        "p"   to "\u0DB4", // ප (pa -> ප)
        "b"   to "\u0DB6", // බ (ba -> බ)
        "m"   to "\u0DB8", // ම (ma -> ම)
        "y"   to "\u0DBA", // ය (ya -> ය)
        "r"   to "\u0DBB", // ර (ra -> ර)
        "l"   to "\u0DBD", // ල (la -> ල)
        "w"   to "\u0DC0", // ව (wa -> ව)
        "v"   to "\u0DC0", // ව (va -> ව)
        "s"   to "\u0DC3", // ස (sa -> ස)
        "h"   to "\u0DC4", // හ (ha -> හ)
        "f"   to "\u0DC6", // ෆ (fa -> ෆ)
        "c"   to "\u0DA0"  // ච
    )

    /**
     * Parses a raw Latin Singlish string into Sinhala Unicode in real-time.
     */
    fun parse(input: String): String {
        if (input.isEmpty()) return ""

        val result = StringBuilder()
        var i = 0
        val len = input.length

        while (i < len) {
            val ch = input[i]

            // 1. Direct Binduva / Visargaya / Gayanukitta / azn
            if (ch == 'x') {
                result.append("\u0D82") // ං
                i++
                continue
            }
            if (ch == 'X') {
                result.append("\u0D9E") // ඞ
                i++
                continue
            }
            if (ch == 'H') {
                result.append("\u0D83") // ඃ
                i++
                continue
            }
            if (input.startsWith("azn", i)) {
                result.append("\u0D82") // ං
                i += 3
                continue
            }

            val subFromI = input.substring(i)

            // 2. Check Consonant
            val consonantMatch = findConsonant(subFromI)
            if (consonantMatch != null) {
                val cPattern = consonantMatch.first
                val cChar = consonantMatch.second
                val nextIdx = i + cPattern.length

                // First check if there is a vowel modifier directly (e.g. kru -> කෘ, kruu -> කෲ, kaa -> කා)
                val vowelModifierMatch = findVowelModifier(input.substring(nextIdx))
                if (vowelModifierMatch != null) {
                    result.append(cChar).append(vowelModifierMatch.second)
                    i = nextIdx + vowelModifierMatch.first.length
                    continue
                }

                // Check for Yansaya: Consonant + 'y' + Vowel (e.g. kya -> ක්‍ය, ky -> ක්‍ය්)
                if (nextIdx < len && (input[nextIdx] == 'y' || input[nextIdx] == 'Y')) {
                    val afterY = nextIdx + 1
                    val vMatch = findVowelModifier(input.substring(afterY))
                    if (vMatch != null) {
                        result.append(cChar).append(HAL).append(ZWJ).append("\u0DBA").append(vMatch.second)
                        i = afterY + vMatch.first.length
                    } else {
                        result.append(cChar).append(HAL).append(ZWJ).append("\u0DBA").append(HAL)
                        i = afterY
                    }
                    continue
                }

                // Check for Rakaransaya: Consonant + 'r' + Vowel (e.g. kra -> ක්‍ර, kr -> ක්‍ර්)
                if (nextIdx < len && (input[nextIdx] == 'r' || input[nextIdx] == 'R')) {
                    val afterR = nextIdx + 1
                    val vMatch = findVowelModifier(input.substring(afterR))
                    if (vMatch != null) {
                        result.append(cChar).append(HAL).append(ZWJ).append("\u0DBB").append(vMatch.second)
                        i = afterR + vMatch.first.length
                    } else {
                        result.append(cChar).append(HAL).append(ZWJ).append("\u0DBB").append(HAL)
                        i = afterR
                    }
                    continue
                }

                // No vowel follows -> pure hal character
                result.append(cChar).append(HAL)
                i = nextIdx
                continue
            }

            // 3. Check Independent Vowel
            val independentVowelMatch = findIndependentVowel(subFromI)
            if (independentVowelMatch != null) {
                result.append(independentVowelMatch.second)
                i += independentVowelMatch.first.length
                continue
            }

            // 4. Any other character (space, punctuation, numbers, etc.)
            result.append(ch)
            i++
        }

        return result.toString()
    }

    private fun findConsonant(str: String): Pair<String, String>? {
        if (str.isEmpty()) return null
        for (c in consonantRules) {
            if (str.startsWith(c.first)) {
                return c
            }
        }
        return null
    }

    private fun findVowelModifier(str: String): Pair<String, String>? {
        if (str.isEmpty()) return null
        for (v in vowelModifierRules) {
            if (str.startsWith(v.first)) {
                return v
            }
        }
        return null
    }

    private fun findIndependentVowel(str: String): Pair<String, String>? {
        if (str.isEmpty()) return null
        for (iv in independentVowelRules) {
            if (str.startsWith(iv.first)) {
                return iv
            }
        }
        return null
    }
}
