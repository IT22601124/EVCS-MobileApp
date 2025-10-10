package com.example.evcs_mobileapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "slots")
data class SlotEntity(
    @PrimaryKey(autoGenerate = true) val slotId: Long = 0,
    val scheduleId: String?,
    val start: String?,
    val end: String?,
    val available: Boolean?,
    val capacity: Int?
)

