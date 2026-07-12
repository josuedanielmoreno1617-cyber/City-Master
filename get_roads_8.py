with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[760:820]):
    print(f"{760 + i}: {line}", end='')
