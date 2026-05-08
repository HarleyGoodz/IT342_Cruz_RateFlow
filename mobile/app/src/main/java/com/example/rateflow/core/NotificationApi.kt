package com.example.rateflow.core

import com.example.rateflow.notifications.Notification
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface NotificationApi {
    // User notification endpoints (using your new UserNotificationController)
    @GET("api/user-notifications")
    fun getUserNotifications(): Call<List<Notification>>

    @DELETE("api/user-notifications/delete/{notificationId}")
    fun deleteUserNotification(@Path("notificationId") notificationId: Int): Call<Map<String, Boolean>>

    @DELETE("api/user-notifications/clear-all")
    fun clearAllUserNotifications(): Call<Map<String, Boolean>>
}