with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[820:1440]):
    print(f"{820 + i}: {line}", end='')
