package com.example.data

import kotlinx.coroutines.flow.Flow

class CityRepository(private val dao: CityDao) {
    val cityState: Flow<CityStateEntity?> = dao.getCityState()
    val buildings: Flow<List<BuildingEntity>> = dao.getBuildings()

    suspend fun updateState(state: CityStateEntity) {
        dao.updateCityState(state)
    }

    suspend fun insertBuilding(building: BuildingEntity) {
        dao.insertBuilding(building)
    }

    suspend fun deleteBuildingAt(x: Int, y: Int) {
        dao.deleteBuildingAt(x, y)
    }
}
