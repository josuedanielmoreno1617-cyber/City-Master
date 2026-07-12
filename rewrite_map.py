import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

# We want to replace the rendering inside the Box that has rotationX/Z.
