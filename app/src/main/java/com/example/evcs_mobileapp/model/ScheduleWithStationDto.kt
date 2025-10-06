package com.example.evcs_mobileapp.model

import com.example.evcs_mobileapp.model.StationDto
import com.example.evcs_mobileapp.model.ScheduleSlotDto

// Data class for schedule with station
data class ScheduleWithStationDto(
    val id: String,
    val stationId: String,
    val date: String,
    val slots: List<ScheduleSlotDto>,
    val station: StationDto
)

