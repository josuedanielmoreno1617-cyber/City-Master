with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

# Fix the compilation error
content = content.replace(
    'val mapTotalWidth = (gridCols + gridRows) * (isoW / 2)',
    'val mapTotalWidth = (isoW / 2) * (gridCols + gridRows)'
)
content = content.replace(
    'val mapTotalHeight = (gridCols + gridRows) * (isoH / 2) + (tileH - isoH)',
    'val mapTotalHeight = (isoH / 2) * (gridCols + gridRows) + (tileH - isoH)'
)

# Fix the missing closing brace
content = content.replace(
    '// Lower category tabs and construction tools',
    '}\n                // Lower category tabs and construction tools'
)

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)

