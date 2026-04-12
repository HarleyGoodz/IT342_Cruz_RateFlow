package com.example.rateflow.network

import com.example.rateflow.model.Service
import retrofit2.Call
import retrofit2.http.GET

interface ServiceApi {

    @GET("api/services")
    fun getAllServices(): Call<List<Service>>
}