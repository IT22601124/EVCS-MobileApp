package com.example.evcs_mobileapp.screens.homescreen

import androidx.room.*

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings")
    suspend fun getAll(): List<BookingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg bookings: BookingEntity)

    @Delete
    suspend fun delete(booking: BookingEntity)
}

