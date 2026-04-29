package com.example.rateflow.model

data class RatingResponse(
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val hasRated: Boolean? = null,
    val rating: Rating? = null
)