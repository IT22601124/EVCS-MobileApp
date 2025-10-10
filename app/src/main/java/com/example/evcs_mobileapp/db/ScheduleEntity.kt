package com.example.evcs_mobileapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val stationId: String?,
    val date: String?
)

