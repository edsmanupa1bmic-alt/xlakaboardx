import re

with open('app/src/main/kotlin/org/fossify/keyboard/services/SimpleKeyboardIME.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.fossify.keyboard.helpers.KeyboardDimensionManager"""

if "import androidx.core.view.ViewCompat" not in content:
    content = content.replace("import android.view.View", f"import android.view.View\n{imports}")

# Find onCreateInputView
on_create_view_pattern = r'override fun onCreateInputView\(\): View \{([^\}]+return inputView\n    \})'
match = re.search(on_create_view_pattern, content, re.DOTALL)

if match:
    original_body = match.group(0)
    
    insets_code = """        // 6. Navigation Bar Insets Fix
        ViewCompat.setOnApplyWindowInsetsListener(inputView) { view, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val defaultPadding = KeyboardDimensionManager.dpToPx(this, 8f)
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                navInsets.bottom.coerceAtLeast(defaultPadding)
            )
            insets
        }
        
        return inputView
    }"""
    
    new_body = original_body.replace("return inputView\n    }", insets_code)
    
    content = content.replace(original_body, new_body)
    
    with open('app/src/main/kotlin/org/fossify/keyboard/services/SimpleKeyboardIME.kt', 'w') as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Could not find onCreateInputView")

