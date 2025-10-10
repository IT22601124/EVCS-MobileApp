package com.example.evcs_mobileapp.model

data class AuthResponse(
    val nic: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val isActive: Boolean? = null,
    val role: String? = null,
    val token: String? = null,
    val expiresAt: String? = null,
    val username: String? = null,
    val isOwner: Boolean? = null,
    val ownerNic: String? = null
)

