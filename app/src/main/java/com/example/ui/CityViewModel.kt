package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BuildingEntity
import com.example.data.CityRepository
import com.example.data.CityStateEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CityUiState(
    val money: Int = 1000,
    val population: Int = 0,
    val day: Int = 1,
    val happiness: Int = 80,
    val power: Int = 0,
    val water: Int = 0,
    val pollution: Int = 0,
    val level: Int = 1,
    val buildings: List<BuildingEntity> = emptyList(),
    val isInitialized: Boolean = false,
    val isIsometricMode: Boolean = true,
    val isGraphicsAdvanced: Boolean = true,
    val dayTimeProgress: Float = 0.2f // 0f to 1f representation
)

enum class BuildingType(
    val displayName: String,
    val cost: Int,
    val colorHex: String,
    val description: String,
    val popProvide: Int = 0,
    val energyProvide: Int = 0,
    val waterProvide: Int = 0,
    val revenueProvide: Int = 0,
    val pollutionProduce: Int = 0,
    val happinessBoost: Int = 0,
    val minLevelRequired: Int = 1
) {
    ROAD("Carretera", 10, "#555555", "Camino para transporte. Conecta zonas urbanas.", 0, 0, 0, 0, 0, 0, 1),
    HOUSE("Residencia", 100, "#2ECC71", "Aumenta la población de la ciudad. Requiere agua/luz.", 15, 0, 0, 0, 1, 3, 1),
    FACTORY("Zona Industrial", 250, "#E74C3C", "Produce ingresos diarios muy altos pero genera polución.", 0, 0, 0, 40, 15, -8, 2),
    COMMERCE("Zona Comercial", 180, "#F1C40F", "Suministra servicios urbanos. Genera ingresos y eleva felicidad.", 0, 0, 0, 15, 2, 8, 2),
    PARK("Parque Sostenible", 140, "#27AE60", "Hermosas plazas verdes. Purifican polución y dan felicidad.", 0, 0, 0, 0, -10, 20, 3),
    POWER_PLANT("Térmica Solar", 300, "#E67E22", "Genera energía para tus edificios residenciales y comerciais.", 0, 80, 0, 0, 1, 4, 3),
    ECOLOGIC_WATER("Acueducto", 220, "#2980B9", "Proporciona suministro de agua dulce potable.", 0, 0, 60, 0, 0, 5, 4)
}

class CityViewModel(private val repository: CityRepository) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    private val _isIsometricMode = MutableStateFlow(true)
    private val _isGraphicsAdvanced = MutableStateFlow(true)
    private val _dayTimeProgress = MutableStateFlow(0.2f)

    val uiState: StateFlow<CityUiState> = combine(
        repository.cityState,
        repository.buildings,
        _isInitialized,
        _isIsometricMode,
        _isGraphicsAdvanced,
        _dayTimeProgress
    ) { flowsArray ->
        val state = flowsArray[0] as? CityStateEntity
        @Suppress("UNCHECKED_CAST")
        val buildings = flowsArray[1] as? List<BuildingEntity> ?: emptyList()
        val initialized = flowsArray[2] as? Boolean ?: false
        val iso = flowsArray[3] as? Boolean ?: true
        val adv = flowsArray[4] as? Boolean ?: true
        val timeProgress = flowsArray[5] as? Float ?: 0.2f

        if (state == null) {
            CityUiState(isInitialized = initialized)
        } else {
            CityUiState(
                money = state.money,
                population = state.population,
                day = state.day,
                happiness = state.happiness,
                power = state.power,
                water = state.water,
                pollution = state.pollution,
                level = state.level,
                buildings = buildings,
                isInitialized = initialized,
                isIsometricMode = iso,
                isGraphicsAdvanced = adv,
                dayTimeProgress = timeProgress
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CityUiState()
    )

    init {
        viewModelScope.launch {
            val initialState = repository.cityState.first()
            if (initialState == null) {
                repository.updateState(
                    CityStateEntity(
                        id = 1,
                        money = 1200,
                        population = 0,
                        day = 1,
                        happiness = 80,
                        power = 0,
                        water = 0,
                        pollution = 0,
                        level = 1
                    )
                )
            }
            _isInitialized.value = true
            startGameLoop()
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            var updateTick = 0f
            while (true) {
                delay(100) // update every 100ms
                updateTick += 0.025f // Time cycle increment step
                
                // Let's cycle day time: full day is 4 seconds (updateTick reaches 1.0)
                if (updateTick >= 1.0f) {
                    updateTick = 0f
                    
                    val currentState = uiState.value
                    if (!currentState.isInitialized) continue
                    val activeBuildings = currentState.buildings
                    
                    // 1. Calculate resources produced by constructions
                    var totalPowerProvided = 0
                    var totalWaterProvided = 0
                    var totalPopulation = 0
                    var netPollution = 0
                    
                    var factoryCount = 0
                    var commerceCount = 0
                    var parkCount = 0
                    var houseCount = 0
                    
                    for (b in activeBuildings) {
                        val type = try { BuildingType.valueOf(b.type) } catch (e: Exception) { null } ?: continue
                        totalPowerProvided += type.energyProvide
                        totalWaterProvided += type.waterProvide
                        totalPopulation += type.popProvide
                        netPollution += type.pollutionProduce
                        
                        when (type) {
                            BuildingType.FACTORY -> factoryCount++
                            BuildingType.COMMERCE -> commerceCount++
                            BuildingType.PARK -> parkCount++
                            BuildingType.HOUSE -> houseCount++
                            else -> {}
                        }
                    }
                    
                    // 2. Resource Requirements & Constraints check
                    val energyDemand = activeBuildings.size * 5
                    val waterDemand = houseCount * 12
                    
                    val powerShortage = totalPowerProvided < energyDemand
                    val waterShortage = totalWaterProvided < waterDemand
                    
                    // 3. Dynamic Happiness % Logic
                    var baseHappiness = 85
                    
                    // Boosts
                    baseHappiness += commerceCount * 5
                    baseHappiness += parkCount * 10
                    
                    // Penalties
                    if (netPollution > 0) {
                        baseHappiness -= (netPollution * 1.2f).toInt()
                    }
                    if (powerShortage) {
                        baseHappiness -= 25
                    }
                    if (waterShortage) {
                        baseHappiness -= 20
                    }
                    // Too high ratio of factories relative to parks makes residents sad
                    if (factoryCount > parkCount * 2 + 1) {
                        baseHappiness -= 15
                    }
                    
                    val calculatedHappiness = baseHappiness.coerceIn(10, 100)
                    
                    // If happy, people flock to residents. If sad, population drops (capacity decreases).
                    val happinessScale = calculatedHappiness / 100.0f
                    val currentPop = (totalPopulation * (0.4f + 0.6f * happinessScale)).toInt()
                    
                    // 4. Dynamic Revenue calculation
                    var defaultRevenue = 0
                    for (b in activeBuildings) {
                        val type = try { BuildingType.valueOf(b.type) } catch (e: Exception) { null } ?: continue
                        defaultRevenue += type.revenueProvide
                    }
                    // Population pays taxes based on overall satisfaction
                    val taxesCollected = (currentPop * 2f * happinessScale).toInt()
                    var netIncome = defaultRevenue + taxesCollected
                    
                    // Critical power failure scales back economic facilities
                    if (powerShortage) {
                        netIncome = (netIncome * 0.4f).toInt()
                    }
                    
                    // 5. Level Milestones based on population
                    val calculatedLevel = when {
                        currentPop >= 100 -> 4
                        currentPop >= 60 -> 3
                        currentPop >= 25 -> 2
                        else -> 1
                    }
                    
                    val newState = CityStateEntity(
                        id = 1,
                        money = currentState.money + netIncome,
                        population = currentPop,
                        day = currentState.day + 1,
                        happiness = calculatedHappiness,
                        power = totalPowerProvided - energyDemand,
                        water = totalWaterProvided - waterDemand,
                        pollution = netPollution.coerceAtLeast(0),
                        level = calculatedLevel
                    )
                    repository.updateState(newState)
                }
                
                // Keep shifting sky lighting angle state continuously
                _dayTimeProgress.value = (_dayTimeProgress.value + 0.015f) % 1.0f
            }
        }
    }

    fun build(type: BuildingType, x: Int, y: Int) {
        viewModelScope.launch {
            val state = uiState.value
            if (state.money >= type.cost && state.level >= type.minLevelRequired) {
                repository.deleteBuildingAt(x, y)
                repository.updateState(
                    CityStateEntity(
                        id = 1,
                        money = state.money - type.cost,
                        population = state.population,
                        day = state.day,
                        happiness = state.happiness,
                        power = state.power,
                        water = state.water,
                        pollution = state.pollution,
                        level = state.level
                    )
                )
                repository.insertBuilding(BuildingEntity(type = type.name, x = x, y = y))
            }
        }
    }

    fun demolish(x: Int, y: Int) {
        viewModelScope.launch {
            repository.deleteBuildingAt(x, y)
        }
    }

    fun setIsometricMode(enabled: Boolean) {
        _isIsometricMode.value = enabled
    }

    fun setGraphicsAdvanced(enabled: Boolean) {
        _isGraphicsAdvanced.value = enabled
    }
}

class CityViewModelFactory(private val repository: CityRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
