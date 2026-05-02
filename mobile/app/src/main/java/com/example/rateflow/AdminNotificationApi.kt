package com.example.rateflow.network

import com.example.rateflow.model.AdminNotification
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface AdminNotificationApi {

    @GET("/api/notifications")
    fun getAdminNotifications(): Call<List<AdminNotification>>

    @DELETE("/api/notifications/delete/{id}")
    fun deleteAdminNotification(@Path("id") id: Int): Call<Map<String, Boolean>>

    @DELETE("/api/notifications/clear-all")
    fun clearAllAdminNotifications(): Call<Map<String, Boolean>>

    @GET("/api/notifications/unread-count")
    fun getAdminUnreadCount(): Call<Map<String, Long>>
}