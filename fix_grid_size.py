import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('val step = 100f', 'val step = 250f')

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)
