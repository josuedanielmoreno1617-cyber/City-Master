with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

import re

# 1. Replace background
bg_replacement = """
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7)) // Blanco roto
            .drawBehind {
                // Cuadrícula de referencia muy tenue
                val step = 100f
                val gridColor = Color.LightGray.copy(alpha = 0.3f)
                var x = 0f
                while (x < size.width) {
                    drawLine(gridColor, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), 1f)
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1f)
                    y += step
                }
            }
    ) {
"""
content = re.sub(
    r'Box\(\s*modifier = Modifier\s*\.fillMaxSize\(\)\s*\.background\(MaterialTheme\.colorScheme\.background\)\s*\.drawBehind \{\s*drawRect\(skyBrush\).*?\}\s*\) \{',
    bg_replacement.strip(),
    content,
    flags=re.DOTALL
)

# 2. Remove CloudSceneryOverlay and WeatherOverlay from the background
content = re.sub(r'if \(uiState\.isGraphicsAdvanced\) \{\s*CloudSceneryOverlay\(dayProgress\)\s*\}\s*WeatherOverlay\(uiState\.weather\)', '', content)

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)
