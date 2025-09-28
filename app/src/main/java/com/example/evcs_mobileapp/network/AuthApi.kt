package com.example.evcs_mobileapp.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class LoginResponse(
    val token: String,
    val expiresAt: String,
    val role: String,
    val username: String
)

interface AuthApi {
    @Headers("Content-Type: application/json")
    @POST("Auth/login")
    suspend fun login(@Body payload: LoginPayload): Response<LoginResponse>
}
