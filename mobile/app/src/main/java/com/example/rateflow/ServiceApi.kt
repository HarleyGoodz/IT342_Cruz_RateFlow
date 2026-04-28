package com.example.rateflow.network

import com.example.rateflow.model.Service
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ServiceApi {

    @GET("api/services")
    fun getAllServices(): Call<List<Service>>

    @GET("api/services/{serviceId}")
    fun getServiceById(@Path("serviceId") serviceId: Int): Call<Service>
}