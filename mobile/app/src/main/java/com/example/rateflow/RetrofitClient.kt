package com.example.rateflow.network

import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Create a CookieManager to handle session cookies
    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    // Create OkHttpClient with cookie jar for session management
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // IMPORTANT: Add CookieJar to maintain session
            .cookieJar(JavaNetCookieJar(cookieManager))
            // Add logging interceptor for debugging
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val serviceApi: ServiceApi by lazy {
        retrofit.create(ServiceApi::class.java)
    }

    val ratingApi: RatingApi by lazy {
        retrofit.create(RatingApi::class.java)
    }

    val userApi: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun getBaseUrl(): String = BASE_URL

    // Optional: Method to clear cookies on logout
    fun clearCookies() {
        cookieManager.cookieStore.removeAll()
    }
}