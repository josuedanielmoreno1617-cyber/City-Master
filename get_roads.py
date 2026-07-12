with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[1200:1330]):
    print(f"{1200 + i}: {line}", end='')
