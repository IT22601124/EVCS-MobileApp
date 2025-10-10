package com.example.evcs_mobileapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.evcs_mobileapp.model.StationDto
import com.example.evcs_mobileapp.model.ScheduleSlotDto

class BookingViewModel : ViewModel() {
    var selectedStation: StationDto? = null
    var selectedDate: String? = null
    var selectedSlot: ScheduleSlotDto? = null

    fun setBooking(station: StationDto, date: String, slot: ScheduleSlotDto) {
        selectedStation = station
        selectedDate = date
        selectedSlot = slot
        Log.d("BookingViewModel", "Booking set: $selectedStation, $selectedDate, $selectedSlot")
    }
}
