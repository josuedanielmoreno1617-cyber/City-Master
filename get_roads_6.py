with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[310:450]):
    print(f"{310 + i}: {line}", end='')
