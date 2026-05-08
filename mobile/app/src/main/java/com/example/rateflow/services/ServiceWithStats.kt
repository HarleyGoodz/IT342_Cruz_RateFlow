package com.example.rateflow.services

import com.example.rateflow.services.Service

data class ServiceWithStats(
    val service: Service,
    val averageRating: Double? = null,
    val totalRatings: Int? = null,
    val latestFeedback: String? = null
)