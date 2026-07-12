import re

with open('app/src/main/java/com/example/ui/CityViewModel.kt', 'r') as f:
    content = f.read()

# Add WeatherCondition enum if it doesn't exist
if 'enum class WeatherCondition' not in content:
    weather_enum = """
enum class WeatherCondition(val displayName: String) {
    SUNNY("Soleado"),
    RAINY("Lluvioso"),
    SNOWY("Nevado")
}
"""
    content = content.replace('import androidx.lifecycle.viewModelScope', weather_enum + 'import androidx.lifecycle.viewModelScope')

# Add weather to CityUiState
if 'val weather: WeatherCondition' not in content:
    content = content.replace(
        'val isGraphicsAdvanced: Boolean = true\n)',
        'val isGraphicsAdvanced: Boolean = true,\n    val weather: WeatherCondition = WeatherCondition.SUNNY\n)'
    )

# Add _weatherCondition StateFlow
if 'private val _weatherCondition = MutableStateFlow' not in content:
    content = content.replace(
        'private val _dayTimeProgress = MutableStateFlow(0.2f)',
        'private val _dayTimeProgress = MutableStateFlow(0.2f)\n    private val _weatherCondition = MutableStateFlow(WeatherCondition.SUNNY)'
    )

# Add _weatherCondition to combine
if '_weatherCondition' not in content.split('combine(')[1].split(') {')[0]:
    content = content.replace(
        '        _isGraphicsAdvanced\n    ) { flowsArray ->',
        '        _isGraphicsAdvanced,\n        _weatherCondition\n    ) { flowsArray ->'
    )
    content = content.replace(
        'val adv = flowsArray[6] as Boolean',
        'val adv = flowsArray[6] as Boolean\n            val w = flowsArray[7] as WeatherCondition'
    )
    content = content.replace(
        'isGraphicsAdvanced = adv\n            )',
        'isGraphicsAdvanced = adv,\n                weather = w\n            )'
    )

# Add weather cycling logic in the game loop
if 'Weather cycling' not in content:
    # Let's add it right after day increment or time progress
    weather_logic = """
                // Weather cycling
                if (Math.random() < 0.1) { // 10% chance to change weather each tick
                    val newWeather = WeatherCondition.values().random()
                    if (newWeather != _weatherCondition.value) {
                        _weatherCondition.value = newWeather
                        addEvent("El clima ha cambiado a ${newWeather.displayName}", EventType.INFO)
                    }
                }
"""
    content = content.replace('// Keep shifting sky lighting angle state continuously', weather_logic + '\n                // Keep shifting sky lighting angle state continuously')

# Modify energy consumption in calculateStats based on weather
if 'energyDemand += if (_weatherCondition.value == WeatherCondition.SNOWY)' not in content:
    content = content.replace(
        'val energyDemand = (totalPopulation * 2) + (factoryCount * 15)',
        'var energyDemand = (totalPopulation * 2) + (factoryCount * 15)\n                    if (_weatherCondition.value == WeatherCondition.SNOWY) energyDemand = (energyDemand * 1.5).toInt() // Heating\n                    if (_weatherCondition.value == WeatherCondition.SUNNY) totalPowerProvided = (totalPowerProvided * 1.2).toInt() // Solar bonus'
    )

with open('app/src/main/java/com/example/ui/CityViewModel.kt', 'w') as f:
    f.write(content)

