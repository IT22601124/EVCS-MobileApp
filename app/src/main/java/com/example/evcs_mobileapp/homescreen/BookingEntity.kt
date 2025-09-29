package com.example.evcs_mobileapp.homescreen

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val station: String,
    val startIso: String,
    val durationMin: Int,
    val status: String // Store as String, map to BookingStatus in code
)

