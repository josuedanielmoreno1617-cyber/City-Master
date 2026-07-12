with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()
for i in range(150, 180):
    print(f"{i}: {lines[i]}", end='')
