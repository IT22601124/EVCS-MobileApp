package com.example.evcs_mobileapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<StationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<SlotEntity>)

    @Query("SELECT * FROM stations")
    suspend fun getAllStations(): List<StationEntity>

    @Query("SELECT * FROM schedules WHERE stationId = :stationId")
    suspend fun getSchedulesForStation(stationId: String): List<ScheduleEntity>

    @Query("SELECT * FROM slots WHERE scheduleId = :scheduleId")
    suspend fun getSlotsForSchedule(scheduleId: String): List<SlotEntity>

    @Query("DELETE FROM stations")
    suspend fun clearStations()

    @Query("DELETE FROM schedules")
    suspend fun clearSchedules()

    @Query("DELETE FROM slots")
    suspend fun clearSlots()
}

