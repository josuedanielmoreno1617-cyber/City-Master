with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "val mapTotalWidth" in line:
        for j in range(i-10, i+25):
            print(f"{j+1}: {lines[j]}", end='')
        break
