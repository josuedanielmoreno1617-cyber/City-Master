with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

first_line = lines[0]
if "OvershootInterpolatorpackage" in first_line or "package com.example.uiimport" in first_line:
    lines[0] = "package com.example.ui\nimport android.view.animation.OvershootInterpolator\nimport androidx.compose.animation.core.*\n" + first_line.replace("import android.view.animation.OvershootInterpolatorpackage com.example.uiimport androidx.compose.animation.core.*", "")

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.writelines(lines)
