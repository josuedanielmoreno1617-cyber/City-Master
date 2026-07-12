with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val posX = (x - y) * (isoW / 2) + (gridRows * (isoW / 2))',
    'val posX = (isoW / 2) * (x - y) + (isoW / 2) * gridRows'
)
content = content.replace(
    'val posY = (x + y) * (isoH / 2)',
    'val posY = (isoH / 2) * (x + y)'
)

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)
