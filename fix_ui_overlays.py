import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

# 1. Add missing imports if needed
if "import androidx.compose.material3.ModalNavigationDrawer" not in content:
    content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\nimport androidx.compose.material3.ModalNavigationDrawer\nimport androidx.compose.material3.DrawerValue\nimport androidx.compose.material3.rememberDrawerState\nimport androidx.compose.material3.ModalDrawerSheet")

# 2. Add hamburger menu to the top bar
top_bar_start = content.find("// Header Top Bar with Stats Only")
if top_bar_start != -1:
    row_start = content.find("Row(", top_bar_start)
    if row_start != -1:
        bracket_index = content.find("{", row_start)
        if bracket_index != -1:
            menu_icon = """
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
"""
            content = content[:bracket_index + 1] + menu_icon + content[bracket_index + 1:]

# 3. Remove the inline HUD elements
# Looking for "// HUD overlay over map" down to "// Lower category tabs"
hud_start = content.find("// HUD overlay over map")
lower_tabs_start = content.find("// Lower category tabs and construction tools")
if hud_start != -1 and lower_tabs_start != -1:
    content = content[:hud_start] + content[lower_tabs_start:]

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)

