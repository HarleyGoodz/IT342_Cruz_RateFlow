package com.example.rateflow.network

import com.example.rateflow.model.Service
import com.example.rateflow.model.UserService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ServiceApi {

    @GET("api/services")
    fun getAllServices(): Call<List<Service>>

    @GET("api/services/{serviceId}")
    fun getServiceById(@Path("serviceId") serviceId: Int): Call<Service>


    // Create Service with image
    @Multipart
    @POST("/api/services/create")
    fun createService(
        @Part("serviceName") serviceName: RequestBody,
        @Part("serviceCategory") serviceCategory: RequestBody,
        @Part("serviceDescription") serviceDescription: RequestBody,
        @Part("createdBy") createdBy: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<Void>
}