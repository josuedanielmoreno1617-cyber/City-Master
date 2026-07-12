with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "Lower category tabs and construction tools" in line:
        print(f"Found at line {i+1}")
        print(f"Lines around: ")
        for j in range(i-10, i+2):
            print(f"{j+1}: {lines[j]}", end='')
        break
