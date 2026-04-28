package com.example.rateflow.network

import com.example.rateflow.RegisterRequest
import com.example.rateflow.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val id: Int?,
    val username: String?,
    val email: String?,
    val role: String?
)

interface ApiService {

    @POST("/api/auth/admin/login")
    fun loginUser(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("/api/auth/register")
    fun registerUser(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>
}

