package com.example.evcs_mobileapp.model

// Top-level model for API response
// Represents a station with its schedules

data class StationWithSchedulesDto(
    val id: String,
    val name: String?,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val slots: Int,
    val isActive: Boolean,
    val schedules: List<ScheduleDto>
)

// Schedule model for each station

data class ScheduleDto(
    val id: String,
    val stationId: String,
    val date: String,
    val slots: List<ScheduleSlotDto>
)

