package com.example.evcs_mobileapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.evcs_mobileapp.db.BookingEntity
import com.example.evcs_mobileapp.db.BookingDao

@Database(entities = [AuthResponseEntity::class, BookingEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authResponseDao(): AuthResponseDao
    abstract fun bookingDao(): BookingDao
}
