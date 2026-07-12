with open('app/src/main/java/com/example/ui/CityViewModel.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "val adv = flowsArray[4] as? Boolean ?: true" in line:
        lines.insert(i+1, '        val weatherCond = flowsArray[5] as? WeatherCondition ?: WeatherCondition.SUNNY\n')
        break

for i, line in enumerate(lines):
    if "isGraphicsAdvanced = adv," in line:
        lines[i] = "                isGraphicsAdvanced = adv,\n                weather = weatherCond\n"
        break

with open('app/src/main/java/com/example/ui/CityViewModel.kt', 'w') as f:
    f.writelines(lines)
