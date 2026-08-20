import re

with open("app/src/main/kotlin/org/fossify/keyboard/helpers/ThemeEngine.kt", "r") as f:
    content = f.read()

# Remove the unused constants
constants_to_remove = [
    "val THEME_DYNAMIC_COLOR",
    "val THEME_SYSTEM_AUTO",
    "val THEME_MATERIAL_LIGHT",
    "val THEME_CYBERPUNK",
    "val THEME_OLED_BLACK",
    "val THEME_EMERALD",
    "val THEME_SUNSET",
    "val THEME_COLOR_RED",
    "val THEME_COLOR_BLUE",
    "val THEME_COLOR_PURPLE",
    "val THEME_COLOR_AMBER",
    "val THEME_COLOR_TEAL",
    "val THEME_LANDSCAPE_MOUNTAINS",
    "val THEME_LANDSCAPE_AURORA",
    "val THEME_LANDSCAPE_OCEAN"
]

lines = content.split('\n')
new_lines = []
for line in lines:
    if any(line.strip().startswith(c) for c in constants_to_remove):
        continue
    new_lines.append(line)

with open("app/src/main/kotlin/org/fossify/keyboard/helpers/ThemeEngine.kt", "w") as f:
    f.write('\n'.join(new_lines))
