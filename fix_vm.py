import re

with open('app/src/main/java/com/example/ui/CityViewModel.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
enum_content = """
enum class WeatherCondition(val displayName: String) {
    SUNNY("Soleado"),
    RAINY("Lluvioso"),
    SNOWY("Nevado")
}
"""

in_enum = False
for line in lines:
    if "enum class WeatherCondition" in line:
        in_enum = True
    
    if in_enum:
        if line.strip() == "}":
            in_enum = False
        continue
    
    new_lines.append(line)

# find data class CityUiState and insert enum before it
for i, line in enumerate(new_lines):
    if "data class CityUiState" in line:
        new_lines.insert(i, enum_content)
        break

# fix weather = w bug
for i, line in enumerate(new_lines):
    if "val adv = flowsArray[6] as Boolean" in line:
        new_lines[i] = "            val adv = flowsArray[6] as Boolean\n            val w = flowsArray[7] as WeatherCondition\n"
    if "val w = flowsArray[7]" in line and "val adv =" not in new_lines[i-1]:
        # remove previous incorrect insertion
        new_lines[i] = ""

with open('app/src/main/java/com/example/ui/CityViewModel.kt', 'w') as f:
    f.writelines(new_lines)
