package com.example.evcs_mobileapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BookingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)

    @Query("SELECT * FROM bookings WHERE nic = :nic ORDER BY date DESC")
    suspend fun getBookingsByNic(nic: String): List<BookingEntity>

    @Query("DELETE FROM bookings")
    suspend fun clearBookings()
}

