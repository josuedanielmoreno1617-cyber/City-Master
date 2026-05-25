package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_state")
data class CityStateEntity(
    @PrimaryKey val id: Int = 1,
    val money: Int,
    val population: Int,
    val day: Int,
    val happiness: Int = 80,
    val power: Int = 0,
    val water: Int = 0,
    val pollution: Int = 0,
    val level: Int = 1
)
