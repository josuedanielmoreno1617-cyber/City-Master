with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[280:310]):
    if "roadConnections" in line:
        print(f"{280 + i}: {line}", end='')
