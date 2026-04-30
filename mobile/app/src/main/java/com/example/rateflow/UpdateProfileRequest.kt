package com.example.rateflow

data class UpdateProfileRequest(
    val username: String  // Only sending username since that's all you're updating
)