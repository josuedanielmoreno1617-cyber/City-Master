with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    text = f.read()

text = text.replace('import android.view.animation.OvershootInterpolator\npackage com.example.ui', 'package com.example.ui\nimport android.view.animation.OvershootInterpolator')

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(text)
