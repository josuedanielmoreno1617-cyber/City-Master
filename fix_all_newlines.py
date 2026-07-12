with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    text = f.read()

# Just regex out all "import" that don't have a newline before them
import re
text = re.sub(r'([^\n])import ', r'\1\nimport ', text)
text = re.sub(r'OvershootInterpolatorimport', r'OvershootInterpolator\nimport', text)
text = re.sub(r'uiimport', r'ui\nimport', text)

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(text)
