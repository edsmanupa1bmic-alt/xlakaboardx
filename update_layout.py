import re

with open('app/src/main/res/layout/keyboard_root.xml', 'r') as f:
    content = f.read()

# 1. Root Container
content = content.replace(
'''    <LinearLayout
        android:id="@+id/keyboard_content_layer"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="@android:color/transparent"
        android:paddingBottom="8dp">''',
'''    <LinearLayout
        android:id="@+id/keyboard_content_layer"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="@android:color/transparent"
        android:paddingTop="6dp"
        android:paddingBottom="8dp">''')

# Row 0
content = content.replace(
'''        <LinearLayout
            android:id="@+id/flashboard_utility_bar"
            android:layout_width="match_parent"
            android:layout_height="42dp"
            android:orientation="horizontal"
            android:gravity="center"
            android:background="@android:color/transparent">''',
'''        <LinearLayout
            android:id="@+id/flashboard_utility_bar"
            android:layout_width="match_parent"
            android:layout_height="42dp"
            android:layout_marginBottom="4dp"
            android:paddingHorizontal="6dp"
            android:orientation="horizontal"
            android:gravity="center"
            android:background="@android:color/transparent">''')

# Utility chips margin
content = re.sub(r'android:layout_margin="6dp"', 'android:layout_marginHorizontal="2dp"', content)

# Row 1
content = content.replace(
'''        <HorizontalScrollView
            android:id="@+id/flashboard_emoji_ribbon"
            android:layout_width="match_parent"
            android:layout_height="36dp"
            android:scrollbars="none"
            android:background="@android:color/transparent">''',
'''        <HorizontalScrollView
            android:id="@+id/flashboard_emoji_ribbon"
            android:layout_width="match_parent"
            android:layout_height="36dp"
            android:layout_marginBottom="6dp"
            android:scrollbars="none"
            android:background="@android:color/transparent">''')

# Row 2
content = content.replace(
'''        <LinearLayout
            android:id="@+id/row_numbers"
            android:layout_width="match_parent"
            android:layout_height="44dp"
            android:orientation="horizontal"
            android:background="@android:color/transparent">''',
'''        <LinearLayout
            android:id="@+id/row_numbers"
            android:layout_width="match_parent"
            android:layout_height="42dp"
            android:layout_marginBottom="4dp"
            android:paddingHorizontal="4dp"
            android:orientation="horizontal"
            android:background="@android:color/transparent">''')

# Row 3
content = content.replace(
'''        <LinearLayout
            android:id="@+id/row_qwerty"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:orientation="horizontal"
            android:background="@android:color/transparent">''',
'''        <LinearLayout
            android:id="@+id/row_qwerty"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginBottom="4dp"
            android:paddingHorizontal="2dp"
            android:orientation="horizontal"
            android:background="@android:color/transparent">''')

# Row 4
content = content.replace(
'''        <LinearLayout
            android:id="@+id/row_home"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:orientation="horizontal"
            android:paddingHorizontal="12dp"
            android:background="@android:color/transparent">''',
'''        <LinearLayout
            android:id="@+id/row_home"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginBottom="4dp"
            android:orientation="horizontal"
            android:paddingHorizontal="14dp"
            android:background="@android:color/transparent">''')

# Row 5
content = content.replace(
'''        <LinearLayout
            android:id="@+id/row_bottom"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:orientation="horizontal"
            android:background="@android:color/transparent">''',
'''        <LinearLayout
            android:id="@+id/row_bottom"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginBottom="6dp"
            android:paddingHorizontal="2dp"
            android:orientation="horizontal"
            android:background="@android:color/transparent">''')

# Row 6
content = content.replace(
'''        <LinearLayout
            android:id="@+id/row_action"
            android:layout_width="match_parent"
            android:layout_height="52dp"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:background="@android:color/transparent">''',
'''        <LinearLayout
            android:id="@+id/row_action"
            android:layout_width="match_parent"
            android:layout_height="52dp"
            android:paddingHorizontal="6dp"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:background="@android:color/transparent">''')

# FlashboardKeyView marginHorizontal
# Add android:layout_marginHorizontal="2dp" if not present
# Some already have android:layout_marginHorizontal="8dp" (spacebar), we should not replace it.
# So we only add it where there is no android:layout_marginHorizontal.

def add_margin(match):
    tag = match.group(0)
    if 'android:layout_marginHorizontal' not in tag:
        # insert it after android:layout_height="..."
        tag = re.sub(r'(android:layout_height="[^"]+")', r'\1\n                android:layout_marginHorizontal="2dp"', tag, count=1)
    return tag

content = re.sub(r'<org\.fossify\.keyboard\.views\.FlashboardKeyView[^>]+/>', add_margin, content)

with open('app/src/main/res/layout/keyboard_root.xml', 'w') as f:
    f.write(content)

print("Done")
