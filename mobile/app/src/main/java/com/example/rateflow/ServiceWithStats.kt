package com.example.rateflow.model

data class ServiceWithStats(
    val service: Service,
    val averageRating: Double? = null,
    val totalRatings: Int? = null,
    val latestFeedback: String? = null
)