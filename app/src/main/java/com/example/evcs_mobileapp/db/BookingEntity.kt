package com.example.evcs_mobileapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val nic: String,
    val ownerName: String,
    val ownerEmail: String,
    val ownerPhone: String,
    val stationId: String,
    val stationName: String,
    val stationAddress: String,
    val stationType: String,
    val date: String,
    val start: String,
    val end: String,
    val status: String,
    val qrToken: String?,
    val createdAt: String,
    val updatedAt: String?
)
