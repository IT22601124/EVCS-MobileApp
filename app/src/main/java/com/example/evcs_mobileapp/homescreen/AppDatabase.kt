package com.example.evcs_mobileapp.homescreen

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BookingEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
}

