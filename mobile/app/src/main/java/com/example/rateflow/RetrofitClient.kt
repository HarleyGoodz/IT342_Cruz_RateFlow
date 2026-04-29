package com.example.rateflow.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val serviceApi: ServiceApi by lazy {
        retrofit.create(ServiceApi::class.java)
    }

    // Add this line for Rating API
    val ratingApi: RatingApi by lazy {
        retrofit.create(RatingApi::class.java)
    }

    fun getBaseUrl(): String = BASE_URL
}