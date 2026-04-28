package com.example.rateflow.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "http://10.0.2.2:8080/"

    // Create ONE Retrofit instance
    private val retrofit: Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    // Existing API (Login, Register, etc.)
    val instance: ApiService by lazy {

        retrofit.create(ApiService::class.java)
    }

    // NEW Service API (for AdminDashboard)
    val serviceApi: ServiceApi by lazy {

        retrofit.create(ServiceApi::class.java)
    }

    // Helper method to get base URL for image loading
    fun getBaseUrl(): String = BASE_URL
}