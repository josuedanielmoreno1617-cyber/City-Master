package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CityStateEntity::class, BuildingEntity::class], version = 1, exportSchema = false)
abstract class CityDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
}
