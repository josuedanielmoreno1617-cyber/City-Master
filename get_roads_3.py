with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[870:930]):
    print(f"{870 + i}: {line}", end='')
