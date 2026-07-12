import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

# 1. We need to find the `Column` inside `Box` that contains the `Header Top Bar with Stats Only` and all overlays.
# Let's locate the Box that contains `CloudSceneryOverlay`
start_marker = "        if (uiState.isGraphicsAdvanced) {"
end_marker = "            // Bottom Build Menu"

# We'll replace everything between start_marker and end_marker with our new Drawer and 3D Viewport logic.
