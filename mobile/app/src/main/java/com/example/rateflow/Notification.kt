package com.example.rateflow.model

data class Notification(
    val id: Int,
    val message: String,
    val type: String,
    val adminId: Int?,
    val adminUsername: String?,
    val userId: Int?,
    val userEmail: String?,
    val actorName: String?,
    val createdAt: String,  // LocalDateTime from backend
    val details: String?,
    val notificationType: String  // "ADMIN" or "USER"
)