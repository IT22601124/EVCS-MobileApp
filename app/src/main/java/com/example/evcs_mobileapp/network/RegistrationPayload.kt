package com.example.evcs_mobileapp.network

data class RegistrationPayload(
    val nic: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String
)

