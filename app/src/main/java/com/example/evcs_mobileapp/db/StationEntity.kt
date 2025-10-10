package com.example.evcs_mobileapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val type: String?,
    val slots: Int?,
    val isActive: Boolean?
)

