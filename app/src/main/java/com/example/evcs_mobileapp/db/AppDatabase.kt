package com.example.evcs_mobileapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.evcs_mobileapp.db.BookingEntity
import com.example.evcs_mobileapp.db.BookingDao
import com.example.evcs_mobileapp.db.StationEntity
import com.example.evcs_mobileapp.db.ScheduleEntity
import com.example.evcs_mobileapp.db.SlotEntity
import com.example.evcs_mobileapp.db.ScheduleDao

@Database(entities = [AuthResponseEntity::class, BookingEntity::class, StationEntity::class, ScheduleEntity::class, SlotEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authResponseDao(): AuthResponseDao
    abstract fun bookingDao(): BookingDao
    abstract fun scheduleDao(): ScheduleDao
}
