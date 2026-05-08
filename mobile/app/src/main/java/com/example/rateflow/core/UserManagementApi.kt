package com.example.rateflow.core

import com.example.rateflow.auth.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserManagementApi {
    @GET("/api/auth/users")
    fun getUsers(): Call<List<User>>

    @PUT("/api/auth/grant-admin/{userId}")
    fun grantAdminAccess(@Path("userId") userId: Int): Call<User>

    @PUT("/api/auth/remove-admin/{userId}")
    fun removeAdminAccess(@Path("userId") userId: Int): Call<User>
}