with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "fun Isometric3DTile(" in line:
        for j in range(i, i+80):
            print(f"{j+1}: {lines[j]}", end='')
        break
