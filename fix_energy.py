import re
with open('app/src/main/java/com/example/ui/CityViewModel.kt', 'r') as f:
    content = f.read()

# Fix totalPowerProvided to be var if it's val
content = content.replace('val totalPowerProvided =', 'var totalPowerProvided =')

# Replace the energyDemand line
content = content.replace(
    'val energyDemand = activeBuildings.size * 5',
    'var energyDemand = activeBuildings.size * 5\n                    if (_weatherCondition.value == WeatherCondition.SNOWY) energyDemand = (energyDemand * 1.5).toInt() // Heating\n                    if (_weatherCondition.value == WeatherCondition.SUNNY) totalPowerProvided = (totalPowerProvided * 1.2).toInt() // Solar bonus'
)

with open('app/src/main/java/com/example/ui/CityViewModel.kt', 'w') as f:
    f.write(content)
