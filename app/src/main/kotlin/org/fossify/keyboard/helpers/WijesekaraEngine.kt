package org.fossify.keyboard.helpers

object WijesekaraEngine {

    private val directMap = mapOf(
        'q' to "ු", 'w' to "අ", 'e' to "ැ", 'r' to "ර", 't' to "එ",
        'y' to "හ", 'u' to "ම", 'i' to "ස", 'o' to "ද", 'p' to "ච",
        'a' to "්", 's' to "ි", 'd' to "ා", 'f' to "ෙ", 'g' to "ට",
        'h' to "ය", 'j' to "ව", 'k' to "න", 'l' to "ක", ';' to "ත",
        '\'' to ".", 'z' to "ූ", 'x' to "ං", 'c' to "ජ", 'v' to "ඩ",
        'b' to "ඉ", 'n' to "බ", 'm' to "ප", ',' to "ල", '.' to "ග",
        '/' to "/",
        // Shifted layer
        'Q' to "ූ", 'W' to "උ", 'E' to "ෑ", 'R' to "ඍ", 'T' to "ඔ",
        'Y' to "ශ", 'U' to "ඹ", 'I' to "ෂ", 'O' to "ධ", 'P' to "ඡ",
        'A' to "ෟ", 'S' to "ී", 'D' to "ෲ", 'F' to "ෆ", 'G' to "ඨ",
        'H' to "්‍ය", 'J' to "ළු", 'K' to "ණ", 'L' to "ඛ", ':' to "ථ",
        'Z' to "ෳ", 'X' to "ඞ", 'C' to "ඣ", 'V' to "ඪ", 'B' to "ඊ",
        'N' to "භ", 'M' to "ඵ", '<' to "ළ", '>' to "ඝ", '?' to "?"
    )

    fun getSinhalaChar(ch: Char): String {
        return directMap[ch] ?: ch.toString()
    }

    fun processKey(currentWord: String, newChar: Char): Pair<Int, String>? {
        val mapped = getSinhalaChar(newChar)
        return Pair(1, mapped)
    }
}
