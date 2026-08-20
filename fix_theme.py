import re

with open("app/src/main/kotlin/org/fossify/keyboard/helpers/ThemeEngine.kt", "r") as f:
    content = f.read()

# Match the entire builtInThemes declaration until the private fun getThemesDir
pattern = r"val builtInThemes: List<ParsedFlorisTheme> = listOf\(.*?\)\s*private fun getThemesDir"
replacement = """val builtInThemes: List<ParsedFlorisTheme> = listOf(
        ParsedFlorisTheme(
            id = "builtin_default_dark",
            name = "Flashboard Default",
            author = "Flashboard",
            isBuiltIn = true,
            isNight = true,
            keyboardBgColor = Color.parseColor("#131316"),
            keyBgColor = Color.parseColor("#23242A"),
            keyBgPressedColor = Color.parseColor("#373942"),
            keyTextColor = Color.parseColor("#FFFFFF"),
            accentKeyBgColor = Color.parseColor("#0066FF"),
            accentKeyTextColor = Color.parseColor("#FFFFFF"),
            smartbarBgColor = Color.parseColor("#1B1C22"),
            smartbarTextColor = Color.parseColor("#FFFFFF"),
            strokeColor = Color.parseColor("#22FFFFFF"),
            strokeWidth = 1,
            cornerRadius = 10f
        )
    )

    private fun getThemesDir"""

new_content = re.sub(pattern, replacement, content, flags=re.MULTILINE|re.DOTALL)

with open("app/src/main/kotlin/org/fossify/keyboard/helpers/ThemeEngine.kt", "w") as f:
    f.write(new_content)
