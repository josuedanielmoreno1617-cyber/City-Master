import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

# We need to find the Box that has translationX/Y and rotationX/Z.
pattern = r'(Box\(\s*modifier = Modifier\s*\.fillMaxSize\(\)\s*\.graphicsLayer\(\s*scaleX = scale,\s*scaleY = scale,\s*translationX = offsetX,\s*translationY = offsetY,\s*rotationX = 55f,\s*rotationZ = 45f\s*\),\s*contentAlignment = Alignment\.Center\s*\)\s*\{)(.*?)(?=\s*// Lower category tabs and construction tools)'

# We need a new renderer.
new_renderer = """Box(
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
                        if (uiState.isIsometricMode) {
                            val tileW = 120.dp
                            val tileH = 200.dp
                            val isoW = 120.dp
                            val isoH = 60.dp
                            
                            val mapTotalWidth = (gridCols + gridRows) * (isoW / 2)
                            val mapTotalHeight = (gridCols + gridRows) * (isoH / 2) + (tileH - isoH)
                            
                            Box(modifier = Modifier.size(mapTotalWidth, mapTotalHeight)) {
                                for (y in 0 until gridRows) {
                                    for (x in 0 until gridCols) {
                                        val building = uiState.buildings.find { it.x == x && it.y == y }
                                        val buildingType = building?.type
                                        
                                        // Calculate population density
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
                                        val density = (score / 250f).coerceIn(0f, 1f)
                                        
                                        val isRoad = buildingType in listOf("ROAD", "DIRT_ROAD", "HIGHWAY")
                                        val roadConnections = if (isRoad) {
                                            val topH = uiState.buildings.any { it.x == x && it.y == y - 1 && it.type in listOf("ROAD", "DIRT_ROAD", "HIGHWAY") }
                                            val rightH = uiState.buildings.any { it.x == x + 1 && it.y == y && it.type in listOf("ROAD", "DIRT_ROAD", "HIGHWAY") }
                                            val bottomH = uiState.buildings.any { it.x == x && it.y == y + 1 && it.type in listOf("ROAD", "DIRT_ROAD", "HIGHWAY") }
                                            val leftH = uiState.buildings.any { it.x == x - 1 && it.y == y && it.type in listOf("ROAD", "DIRT_ROAD", "HIGHWAY") }
                                            intArrayOf(if(topH) 1 else 0, if(rightH) 1 else 0, if(bottomH) 1 else 0, if(leftH) 1 else 0)
                                        } else null
                                        
                                        val posX = (x - y) * (isoW / 2) + (gridRows * (isoW / 2))
                                        val posY = (x + y) * (isoH / 2)
                                        
                                        Box(
                                            modifier = Modifier
                                                .offset(x = posX, y = posY)
                                                .size(width = tileW, height = tileH)
                                                .clickable { selectedCell = Pair(x, y) }
                                        ) {
                                            Isometric3DTile(
                                                buildingString = buildingType,
                                                dayProgress = dayProgress,
                                                isGraphicsAdvanced = uiState.isGraphicsAdvanced,
                                                isSelected = (selectedCell == Pair(x, y)),
                                                roadConnections = roadConnections,
                                                density = density,
                                                showDensityOverlay = showDensityOverlay
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // 2D View Mode
                            Box(
                                modifier = Modifier
                                    .graphicsLayer(rotationX = 55f, rotationZ = 45f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(cellSize * gridCols)
                                        .height(cellSize * gridRows)
                                        .background(Color(0xFFC8E6C9))
                                        .border(1.dp, Color.Black.copy(alpha=0.3f))
                                ) {
                                    for (y in 0 until gridRows) {
                                        for (x in 0 until gridCols) {
                                            val building = uiState.buildings.find { it.x == x && it.y == y }
                                            val buildingType = building?.let {
                                                try { BuildingType.valueOf(it.type) } catch (e: Exception) { null }
                                            }
                                            
                                            // Calculate population density
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
                                            val density = (score / 250f).coerceIn(0f, 1f)
                                            
                                            val cellBgColor = if (showDensityOverlay && density > 0f) {
                                                when {
                                                    density < 0.2f -> Color(0xFF81C784).copy(alpha = 0.5f)
                                                    density < 0.5f -> Color(0xFFFFB74D).copy(alpha = 0.75f)
                                                    density < 0.8f -> Color(0xFFF06292).copy(alpha = 0.85f)
                                                    else -> Color(0xFFE91E63).copy(alpha = 0.95f)
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
                                                androidx.compose.animation.AnimatedVisibility(
                                                    visible = buildingType != null,
                                                    enter = androidx.compose.animation.scaleIn(animationSpec = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + androidx.compose.animation.fadeIn(),
                                                    exit = androidx.compose.animation.fadeOut()
                                                ) {
                                                    if (buildingType != null) {
                                                        Icon(
                                                            imageVector = get2DIcon(buildingType),
                                                            contentDescription = buildingType.displayName,
                                                            tint = Color.White.copy(alpha = 0.9f),
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                    }
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
                    }
"""

match = re.search(pattern, content, re.DOTALL)
if match:
    # Replace it!
    new_content = content[:match.start()] + new_renderer + content[match.end():]
    with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
        f.write(new_content)
    print("Replaced successfully!")
else:
    print("Pattern not found!")

