import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

# We want to replace from `if (uiState.isIsometricMode) {` down to the `}` before `// HUD overlay over map`
start_marker = "if (uiState.isIsometricMode) {"
end_marker = "// HUD overlay over map"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx == -1 or end_idx == -1:
    print("Markers not found!")
    exit(1)

new_viewport = """// Vista Cenital (Top-Down Horizontal) - 20x15
                var scale by remember { mutableFloatStateOf(1.0f) }
                var offsetX by remember { mutableFloatStateOf(0f) }
                var offsetY by remember { mutableFloatStateOf(0f) }
                
                val minScale = 0.5f
                val maxScale = 4f
                val gridCols = 20
                val gridRows = 15
                val cellSize = 60.dp

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)) // Background for canvas
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(minScale, maxScale)
                                val limitX = 2000f * scale
                                val limitY = 2000f * scale

                                offsetX = (offsetX + pan.x).coerceIn(-limitX, limitX)
                                offsetY = (offsetY + pan.y).coerceIn(-limitY, limitY)
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(cellSize * gridCols)
                                .height(cellSize * gridRows)
                                .background(Color(0xFFC8E6C9)) // Verde claro para terreno vacío
                                .border(1.dp, Color.Black.copy(alpha=0.3f))
                        ) {
                            for (y in 0 until gridRows) {
                                for (x in 0 until gridCols) {
                                    val building = uiState.buildings.find { it.x == x && it.y == y }
                                    val buildingType = building?.let {
                                        try { BuildingType.valueOf(it.type) } catch (e: Exception) { null }
                                    }

                                    // Calculate local population density in 2D
                                    val density = remember(x, y, uiState.buildings) {
                                        var score = 0f
                                        for (b in uiState.buildings) {
                                            val dist = kotlin.math.abs(b.x - x) + kotlin.math.abs(b.y - y)
                                            if (dist <= 1) {
                                                val weight = when (b.type) {
                                                    "SKYSCRAPER" -> 200f
                                                    "HOUSE" -> 20f
                                                    "ZONE_RESIDENTIAL" -> 10f
                                                    else -> 0f
                                                }
                                                val factor = if (dist == 0) 1.0f else 0.5f
                                                score += weight * factor
                                            }
                                        }
                                        (score / 250f).coerceIn(0f, 1f)
                                    }

                                    val cellBgColor = if (showDensityOverlay && density > 0f) {
                                        when {
                                            density < 0.2f -> Color(0xFF81C784).copy(alpha = 0.5f) // Light Green
                                            density < 0.5f -> Color(0xFFFFB74D).copy(alpha = 0.75f) // Orange
                                            density < 0.8f -> Color(0xFFF06292).copy(alpha = 0.85f) // Deep Pink
                                            else -> Color(0xFFE91E63).copy(alpha = 0.95f) // Vibrant Magenta
                                        }
                                    } else if (buildingType != null) {
                                        Color(android.graphics.Color.parseColor(buildingType.colorHex))
                                    } else {
                                        Color.Transparent
                                    }

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .offset(x = cellSize * x, y = cellSize * y)
                                            .size(cellSize)
                                            .background(cellBgColor)
                                            .border(
                                                width = if (selectedCell == Pair(x, y)) 3.dp else 0.5.dp,
                                                color = if (selectedCell == Pair(x, y)) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.15f)
                                            )
                                            .clickable { selectedCell = Pair(x, y) }
                                    ) {
                                        if (buildingType != null) {
                                            Icon(
                                                imageVector = get2DIcon(buildingType),
                                                contentDescription = buildingType.displayName,
                                                tint = Color.White.copy(alpha = 0.9f),
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }

                                        if (density > 0f) {
                                            val densityColor = when {
                                                density < 0.2f -> Color(0xFF4FC3F7)
                                                density < 0.5f -> Color(0xFFFFB74D)
                                                else -> Color(0xFFFF4081)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(4.dp)
                                                    .size(6.dp)
                                                    .background(densityColor, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                """

new_content = content[:start_idx] + new_viewport + content[end_idx:]

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(new_content)

print("Replaced viewport!")
