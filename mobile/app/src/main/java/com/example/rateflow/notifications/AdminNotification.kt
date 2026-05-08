package com.example.rateflow.notifications

import com.google.gson.annotations.SerializedName

data class AdminNotification(
    val id: Int = 0,
    val title: String = "",
    val message: String = "",
    val type: String = "",

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("isRead")
    val isRead: Boolean = false
)