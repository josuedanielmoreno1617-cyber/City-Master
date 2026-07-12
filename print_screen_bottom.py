with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    lines = f.readlines()
for i in range(len(lines)-20, len(lines)):
    print(f"{i}: {lines[i]}", end='')
