with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "var scale by remember { mutableFloatStateOf(" in line:
        for j in range(i-5, i+15):
            print(f"{j+1}: {lines[j]}", end='')
        break
