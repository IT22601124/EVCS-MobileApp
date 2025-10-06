package com.example.evcs_mobileapp.model

// Data class for station
data class StationDto(
    val id: String,
    val name: String?,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val slots: Int,
    val isActive: Boolean
)

