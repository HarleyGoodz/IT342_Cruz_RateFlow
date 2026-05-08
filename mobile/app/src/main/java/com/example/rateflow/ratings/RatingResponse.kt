package com.example.rateflow.ratings

import com.example.rateflow.ratings.Rating

data class RatingResponse(
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val hasRated: Boolean? = null,
    val rating: Rating? = null
)