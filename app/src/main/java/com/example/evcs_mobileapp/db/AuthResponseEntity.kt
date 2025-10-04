package com.example.evcs_mobileapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_response")
data class AuthResponseEntity(
    @PrimaryKey val nic: String,
    val fullName: String?,
    val email: String?,
    val phone: String?,
    val isActive: Boolean?,
    val role: String?,
    val token: String?,
    val expiresAt: String?,
    val username: String?,
    val isOwner: Boolean?,
    val ownerNic: String?
)

