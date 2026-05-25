package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val x: Int,
    val y: Int
)
