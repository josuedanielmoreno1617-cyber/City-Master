with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "isIsometricMode" in line:
        print(f"{i}: {line.strip()}")
