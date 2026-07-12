import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

# Make it isometric again
isometric_replace = """
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY,
                                rotationX = 55f,
                                rotationZ = 45f
                            )
"""
content = re.sub(
    r'\.fillMaxSize\(\)\s*\.graphicsLayer\(\s*scaleX = scale,\s*scaleY = scale,\s*translationX = offsetX,\s*translationY = offsetY\s*\)',
    isometric_replace.strip(),
    content
)

# Add animations to the building placement
building_icon_start = content.find("if (buildingType != null) {\\n                                            Icon(")
# We'll just replace the `if (buildingType != null) { Icon(...) }` with AnimatedVisibility

old_icon_code = """
                                        if (buildingType != null) {
                                            Icon(
                                                imageVector = get2DIcon(buildingType),
                                                contentDescription = buildingType.displayName,
                                                tint = Color.White.copy(alpha = 0.9f),
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
"""
new_icon_code = """
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = buildingType != null,
                                            enter = androidx.compose.animation.scaleIn(animationSpec = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.OvershootInterpolator().let { androidx.compose.animation.core.Easing { fraction -> it.getInterpolation(fraction) } })) + androidx.compose.animation.fadeIn(),
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
"""

if "OvershootInterpolator" in new_icon_code and "import android.view.animation.OvershootInterpolator" not in content:
    content = "import android.view.animation.OvershootInterpolator\n" + content

# Since we don't have exactly `old_icon_code` due to spacing, let's use regex
pattern = r"if\s*\(buildingType\s*!=\s*null\)\s*\{\s*Icon\(\s*imageVector\s*=\s*get2DIcon\(buildingType\),\s*contentDescription\s*=\s*buildingType\.displayName,\s*tint\s*=\s*Color\.White\.copy\(alpha\s*=\s*0\.9f\),\s*modifier\s*=\s*Modifier\.size\(32\.dp\)\s*\)\s*\}"

content = re.sub(pattern, new_icon_code.strip(), content)

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)

