import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

# 1. Add edge parameters to Isometric3DTile
content = content.replace(
    'showDensityOverlay: Boolean = false\n) {',
    'showDensityOverlay: Boolean = false,\n    isEdgeTopLeft: Boolean = false,\n    isEdgeTopRight: Boolean = false,\n    isEdgeBottomLeft: Boolean = false,\n    isEdgeBottomRight: Boolean = false\n) {'
)

# 2. Add border drawing logic inside Isometric3DTile
border_logic = """
        // Draw soft grid lines
        drawPath(topRhombusPath, color = Color(0xFF9FB9AB).copy(alpha = 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
        
        // Draw outer board borders (Dark green, thick)
        val outerBorderColor = Color(0xFF2E7D32)
        val outerBorderWidth = 4f
        
        if (isEdgeTopLeft) {
            drawLine(outerBorderColor, androidx.compose.ui.geometry.Offset(w / 2, gY), androidx.compose.ui.geometry.Offset(w / 2 - rw / 2, gY + rh / 2), strokeWidth = outerBorderWidth)
        }
        if (isEdgeTopRight) {
            drawLine(outerBorderColor, androidx.compose.ui.geometry.Offset(w / 2, gY), androidx.compose.ui.geometry.Offset(w / 2 + rw / 2, gY + rh / 2), strokeWidth = outerBorderWidth)
        }
        if (isEdgeBottomLeft) {
            drawLine(outerBorderColor, androidx.compose.ui.geometry.Offset(w / 2 - rw / 2, gY + rh / 2), androidx.compose.ui.geometry.Offset(w / 2, gY + rh), strokeWidth = outerBorderWidth)
        }
        if (isEdgeBottomRight) {
            drawLine(outerBorderColor, androidx.compose.ui.geometry.Offset(w / 2 + rw / 2, gY + rh / 2), androidx.compose.ui.geometry.Offset(w / 2, gY + rh), strokeWidth = outerBorderWidth)
        }
"""
content = re.sub(
    r'// Draw soft grid lines\s*drawPath\(topRhombusPath, color = Color\(0xFF9FB9AB\)\.copy\(alpha = 0\.5f\), style = androidx\.compose\.ui\.graphics\.drawscope\.Stroke\(width = 1f\)\)',
    border_logic.strip(),
    content,
    flags=re.DOTALL
)

# 3. Pass edge parameters from the Map loop
loop_replacement = """
                                            Isometric3DTile(
                                                buildingString = buildingType,
                                                dayProgress = dayProgress,
                                                isGraphicsAdvanced = uiState.isGraphicsAdvanced,
                                                isSelected = (selectedCell == Pair(x, y)),
                                                roadConnections = roadConnections,
                                                density = density,
                                                showDensityOverlay = showDensityOverlay,
                                                isEdgeTopLeft = (x == 0),
                                                isEdgeTopRight = (y == 0),
                                                isEdgeBottomRight = (x == gridCols - 1),
                                                isEdgeBottomLeft = (y == gridRows - 1)
                                            )
"""
content = re.sub(
    r'Isometric3DTile\(\s*buildingString = buildingType,\s*dayProgress = dayProgress,\s*isGraphicsAdvanced = uiState\.isGraphicsAdvanced,\s*isSelected = \(selectedCell == Pair\(x, y\)\),\s*roadConnections = roadConnections,\s*density = density,\s*showDensityOverlay = showDensityOverlay\s*\)',
    loop_replacement.strip(),
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)
