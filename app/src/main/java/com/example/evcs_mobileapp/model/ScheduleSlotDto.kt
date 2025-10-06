package com.example.evcs_mobileapp.model

data class ScheduleSlotDto(
    val start: String,
    val end: String,
    val available: Boolean,
    val capacity: Int
)

