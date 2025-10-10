package com.example.evcs_mobileapp.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.evcs_mobileapp.model.StationDto
import com.example.evcs_mobileapp.model.ScheduleSlotDto
import com.example.evcs_mobileapp.screens.homescreen.BookingItem

class BookingViewModel : ViewModel() {
    var selectedStation: StationDto? = null
    var selectedDate: String? = null
    var selectedSlot: ScheduleSlotDto? = null
    var bookings by mutableStateOf<List<BookingItem>>(emptyList()) // Use Compose state

    fun setBooking(station: StationDto, date: String, slot: ScheduleSlotDto) {
        selectedStation = station
        selectedDate = date
        selectedSlot = slot
        Log.d("BookingViewModel", "Booking set: $selectedStation, $selectedDate, $selectedSlot")
    }
}
