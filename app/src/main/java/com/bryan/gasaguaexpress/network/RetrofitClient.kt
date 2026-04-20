package com.bryan.gasaguaexpress.network

import com.bryan.gasaguaexpress.models.LoginRequest
import com.bryan.gasaguaexpress.models.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitClient {
    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
}