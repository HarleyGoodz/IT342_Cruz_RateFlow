package com.example.rateflow.model

data class Service(
    val serviceId: Int,
    val serviceName: String,
    val serviceCategory: String,
    val serviceDescription: String,
    val createdBy: String
)