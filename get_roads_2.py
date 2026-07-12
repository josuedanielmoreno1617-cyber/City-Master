with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[960:1040]):
    print(f"{960 + i}: {line}", end='')
