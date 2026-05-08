package com.example.rateflow.network

import com.example.rateflow.auth.RegisterRequest
import com.example.rateflow.auth.RegisterResponse
import com.example.rateflow.profile.UpdateProfileRequest
import com.example.rateflow.auth.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

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

data class GoogleLoginRequest(
    val idToken: String
)

data class GoogleLoginResponse(
    val success: Boolean?,
    val user: User?,
    val role: String?,
    val error: String?,
    val message: String?,
    val email: String?
)

data class GoogleRegisterRequest(
    val email: String,
    val username: String,
    val googleId: String
)

interface ApiService {

    @POST("/api/auth/google")
    fun googleLogin(
        @Body request: GoogleLoginRequest
    ): Call<GoogleLoginResponse>

    @POST("/api/auth/register-from-google")
    fun registerFromGoogle(
        @Body request: GoogleRegisterRequest
    ): Call<RegisterResponse>

    @POST("/api/auth/admin/login")
    fun loginUser(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("/api/auth/register")
    fun registerUser(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    @PUT("/api/auth/update-profile")
    fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Call<User>

    @POST("/api/auth/logout")
    fun logout(): Call<Void>

    @POST("/api/auth/forgot-password")
    fun forgotPassword(
        @Body request: Map<String, String>
    ): Call<Map<String, Any>>

    @POST("/api/auth/reset-password")
    fun resetPassword(
        @Body request: Map<String, String>
    ): Call<Map<String, Any>>

    @GET("/api/auth/validate-reset-token")
    fun validateResetToken(
        @Query("token") token: String
    ): Call<Map<String, Any>>
}



