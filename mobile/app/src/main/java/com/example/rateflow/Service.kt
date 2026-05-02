package com.example.rateflow.model

import java.io.Serializable

data class Service(
    val serviceId: Int,
    val imageUrl: String? = null,
    val serviceName: String,
    val serviceCategory: String,
    val serviceDescription: String,
    val createdBy: String
) : Serializable