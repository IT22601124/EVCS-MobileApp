package com.example.evcs_mobileapp.network

data class SignupPayload(
    val nic: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String = "Owner"
)

