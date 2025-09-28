package com.example.evcs_mobileapp.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RegistrationApi {
    @Headers("Content-Type: application/json")
    @POST("api/Registration/owner")
    suspend fun registerOwner(@Body payload: RegistrationPayload): Response<Void>
}

