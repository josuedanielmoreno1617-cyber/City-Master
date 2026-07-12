with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import android.view.animation.OvershootInterpolatorpackage com.example.ui", "package com.example.ui\nimport android.view.animation.OvershootInterpolator\n")
content = content.replace("uiimport", "ui\nimport")
content = content.replace(".*import", ".*\nimport")
content = content.replace("TextFieldimport", "TextField\nimport")
content = content.replace("backgroundimport", "background\nimport")

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)
