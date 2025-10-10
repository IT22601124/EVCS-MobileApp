package com.example.evcs_mobileapp.network

import com.example.evcs_mobileapp.model.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @Headers("Content-Type: application/json")
    @POST("Auth/login")
    suspend fun login(@Body payload: LoginPayload): Response<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("registration/owner")
    suspend fun signup(@Body payload: SignupPayload): Response<AuthResponse>
}
