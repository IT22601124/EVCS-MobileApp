package com.example.evcs_mobileapp.model

data class BookingItem(
    val id: String,
    val nic: String,
    val ownerName: String? = null,
    val ownerPhone: String? = null,
    val stationId: String,
    val date: String,
    val start: String,
    val end: String,
    val status: String
)