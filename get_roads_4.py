with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[840:870]):
    print(f"{840 + i}: {line}", end='')
