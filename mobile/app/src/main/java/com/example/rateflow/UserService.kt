package com.example.rateflow.model

data class UserService(
    val serviceId: Int,
    val imageUrl: String? = null,
    val serviceName: String,
    val serviceCategory: String,
    val serviceDescription: String,
    val createdBy: String,
    val createdAt: String? = null
)