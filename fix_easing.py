with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(
    "androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.OvershootInterpolator().let { androidx.compose.animation.core.Easing { fraction -> it.getInterpolation(fraction) } })",
    "androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)"
)

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(text)
