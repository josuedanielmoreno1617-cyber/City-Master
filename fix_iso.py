import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

# 1. Update the Map container iso parameters
content = content.replace(
    'val tileH = 200.dp\n                            val isoW = 120.dp\n                            val isoH = 60.dp',
    'val tileH = 200.dp\n                            val isoW = 120.dp\n                            val isoH = 69.28.dp // 30 degrees tilt'
)

# 2. Update Isometric3DTile variables
replacement = """
        // Base of coordinate system inside box
        val rw = w
        val rh = w * 0.57735f // 30 degrees tilt height
        val gY = h - rh // Base flat on the bottom of the container
"""
content = re.sub(
    r'// Base of coordinate system inside box\s*val gY = h \* 0\.65f\s*val rw = w.*?val gt = 14f // ground thickness \(Voxel base depth\)',
    replacement.strip(),
    content,
    flags=re.DOTALL
)

# 3. Remove the side walls
content = re.sub(
    r'// 2\. Draw Side Walls of the Voxel Ground base \(Volumen\).*?drawPath\(rightSoilPath, color = Color\(0xFF9E9E9E\)\.copy\(alpha = 0\.8f \* illuminationCoefficient\)\)',
    '',
    content,
    flags=re.DOTALL
)

# 4. Add the grid line stroke
content = re.sub(
    r'(drawPath\(topRhombusPath, color = surfaceColor\))',
    r'\1\n        // Draw soft grid lines\n        drawPath(topRhombusPath, color = Color(0xFF9FB9AB).copy(alpha = 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))',
    content
)

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)
