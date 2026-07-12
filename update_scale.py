import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

replacement = """
            androidx.compose.foundation.layout.BoxWithConstraints(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val screenW = maxWidth.value
                // For a 20x15 grid with isoW=120, total width is 2100
                val targetScale = (screenW * 0.7f) / 2100f

                // Vista Cenital (Top-Down Horizontal) - 20x15
                var scale by remember { mutableFloatStateOf(targetScale) }
                var offsetX by remember { mutableFloatStateOf(0f) }
                var offsetY by remember { mutableFloatStateOf(0f) }
                
                val minScale = 0.1f
"""

content = re.sub(
    r'Box\(\s*contentAlignment = Alignment\.Center,\s*modifier = Modifier\s*\.weight\(1f\)\s*\.fillMaxWidth\(\)\s*\) \{\s*// Vista Cenital \(Top-Down Horizontal\) - 20x15\s*var scale by remember \{ mutableFloatStateOf\(1\.0f\) \}\s*var offsetX by remember \{ mutableFloatStateOf\(0f\) \}\s*var offsetY by remember \{ mutableFloatStateOf\(0f\) \}\s*val minScale = 0\.5f',
    replacement.strip(),
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)
