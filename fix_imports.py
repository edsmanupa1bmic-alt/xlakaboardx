import re

with open('app/src/main/kotlin/org/fossify/keyboard/services/SimpleKeyboardIME.kt', 'r') as f:
    content = f.read()

# Fix the broken ViewGroup import
content = content.replace("import android.view.View\nimport androidx.core.view.ViewCompat\nimport androidx.core.view.WindowInsetsCompat\nimport org.fossify.keyboard.helpers.KeyboardDimensionManagerGroup", "import android.view.ViewGroup")

# Remove all manual inserted ViewCompat/WindowInsetsCompat and re-add them uniquely
content = re.sub(r'import androidx\.core\.view\.ViewCompat\n', '', content)
content = re.sub(r'import androidx\.core\.view\.WindowInsetsCompat\n', '', content)
content = re.sub(r'import org\.fossify\.keyboard\.helpers\.KeyboardDimensionManager\n', '', content)

content = content.replace("import android.view.View", "import android.view.View\nimport android.view.ViewGroup\nimport androidx.core.view.ViewCompat\nimport androidx.core.view.WindowInsetsCompat\nimport org.fossify.keyboard.helpers.KeyboardDimensionManager\n", 1)

# Ensure no duplicate ViewGroup
content = re.sub(r'import android\.view\.ViewGroup\n+', 'import android.view.ViewGroup\n', content)

with open('app/src/main/kotlin/org/fossify/keyboard/services/SimpleKeyboardIME.kt', 'w') as f:
    f.write(content)

