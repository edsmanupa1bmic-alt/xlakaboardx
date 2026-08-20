import re

with open("app/src/main/kotlin/org/fossify/keyboard/helpers/ThemeEngine.kt", "r") as f:
    content = f.read()

# Replace everything from `val builtInThemes` to the closing paren of `listOf(...)`
pattern = r"val builtInThemes: List<ParsedFlorisTheme> = listOf\(.*?^\s*\)"
replacement = """val builtInThemes: List<ParsedFlorisTheme> = listOf(
        ParsedFlorisTheme(
            id = THEME_DEFAULT_DARK,
            name = "Default Dark",
            author = "Gboard Material 3",
            isBuiltIn = true,
            isNight = true,
            keyboardBgColor = Color.parseColor("#121316"),
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
    )"""

new_content = re.sub(pattern, replacement, content, flags=re.MULTILINE|re.DOTALL)

with open("app/src/main/kotlin/org/fossify/keyboard/helpers/ThemeEngine.kt", "w") as f:
    f.write(new_content)
