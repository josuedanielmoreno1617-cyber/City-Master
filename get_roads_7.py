with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[150:310]):
    print(f"{150 + i}: {line}", end='')
