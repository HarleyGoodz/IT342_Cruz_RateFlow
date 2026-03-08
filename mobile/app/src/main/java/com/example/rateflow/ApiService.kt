package com.example.rateflow

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

data class User(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)



interface ApiService {

    @POST("api/auth/register")
    fun registerUser(@Body user: User): Call<User>

    @POST("api/auth/login")
    fun loginUser(@Body request: LoginRequest): Call<User>

    @GET("api/auth/me")
    fun getCurrentUser(): Call<User>

    @POST("api/auth/logout")
    fun logout(): Call<String>
}
