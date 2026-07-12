import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val posX = (isoW / 2) * (x - y) + (isoW / 2) * gridRows',
    'val posX = (isoW / 2) * (x - y) + (isoW / 2) * (gridRows - 1)'
)

# And fix mapTotalWidth to 2100 (which is (isoW / 2) * (gridCols + gridRows - 1 + 2) = (isoW / 2) * (gridCols + gridRows + 1))
# Wait, let's just make mapTotalWidth exact. 60 * 35 = 2100.
# (20 + 15) = 35. So (isoW / 2) * (gridCols + gridRows) = 2100.
# So mapTotalWidth is correctly 2100! No need to change mapTotalWidth formula.

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)
