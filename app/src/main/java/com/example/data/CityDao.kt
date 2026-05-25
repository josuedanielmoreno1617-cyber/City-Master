package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM city_state WHERE id = 1")
    fun getCityState(): Flow<CityStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCityState(state: CityStateEntity)

    @Query("SELECT * FROM buildings")
    fun getBuildings(): Flow<List<BuildingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: BuildingEntity)

    @Query("DELETE FROM buildings WHERE x = :x AND y = :y")
    suspend fun deleteBuildingAt(x: Int, y: Int)
}
