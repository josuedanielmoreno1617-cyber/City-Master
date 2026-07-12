with open('app/src/main/java/com/example/ui/CityViewModel.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "weather = w" in line:
        continue
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/CityViewModel.kt', 'w') as f:
    f.writelines(new_lines)
