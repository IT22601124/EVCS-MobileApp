package com.example.evcs_mobileapp.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AuthResponseEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authResponseDao(): AuthResponseDao
}

