with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[1485:1510]):
    print(f"{1485 + i}: {line}", end='')
