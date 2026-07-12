with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

# Make skyBrush react to weather
skyBrush_code = """
    val skyBrush = remember(dayProgress, uiState.weather) {
        val color1: Color
        val color2: Color
        when {
            uiState.weather == WeatherCondition.RAINY -> {
                color1 = Color(0xFF424242)
                color2 = Color(0xFF757575)
            }
            uiState.weather == WeatherCondition.SNOWY -> {
                color1 = Color(0xFFCFD8DC)
                color2 = Color(0xFFECEFF1)
            }
            dayProgress < 0.2f -> { // Dawn transition
                val ratio = dayProgress / 0.2f
                color1 = lerpColor(Color(0xFF0F1E36), Color(0xFFFF9E79), ratio)
                color2 = lerpColor(Color(0xFF233A5F), Color(0xFF70A1FF), ratio)
            }
"""

import re
content = re.sub(
    r'val skyBrush = remember\(dayProgress\) \{\s*val color1: Color\s*val color2: Color\s*when \{\s*dayProgress < 0\.2f -> \{ // Dawn transition\s*val ratio = dayProgress / 0\.2f\s*color1 = lerpColor\(Color\(0xFF0F1E36\), Color\(0xFFFF9E79\), ratio\)\s*color2 = lerpColor\(Color\(0xFF233A5F\), Color\(0xFF70A1FF\), ratio\)\s*\}',
    skyBrush_code.strip(),
    content
)

# Add WeatherOverlay component
overlay_comp = """
@Composable
fun WeatherOverlay(weather: WeatherCondition) {
    if (weather == WeatherCondition.SUNNY) return

    val infiniteTransition = rememberInfiniteTransition(label = "weather")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "weather_anim"
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        if (weather == WeatherCondition.RAINY) {
            val numDrops = 150
            for (i in 0 until numDrops) {
                val x = (i * 1234567f) % w
                val startY = ((i * 9876543f) % h + animOffset * h * 1.5f) % h
                drawLine(
                    color = Color(0xFF81D4FA).copy(alpha = 0.6f),
                    start = androidx.compose.ui.geometry.Offset(x, startY),
                    end = androidx.compose.ui.geometry.Offset(x + 5f, startY + 20f),
                    strokeWidth = 2f
                )
            }
        } else if (weather == WeatherCondition.SNOWY) {
            val numFlakes = 100
            for (i in 0 until numFlakes) {
                val x = ((i * 1234567f) % w + animOffset * 100f) % w
                val y = ((i * 9876543f) % h + animOffset * h) % h
                val radius = 3f + (i % 3)
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
    }
}
"""

if "fun WeatherOverlay" not in content:
    content += overlay_comp

# insert WeatherOverlay in the screen
content = content.replace(
    '        if (uiState.isGraphicsAdvanced) {\n            CloudSceneryOverlay(dayProgress)\n        }',
    '        if (uiState.isGraphicsAdvanced) {\n            CloudSceneryOverlay(dayProgress)\n        }\n        WeatherOverlay(uiState.weather)'
)

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)
