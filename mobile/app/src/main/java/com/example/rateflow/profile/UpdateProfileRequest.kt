package com.example.rateflow.profile

data class UpdateProfileRequest(
    val username: String  // Only sending username since that's all you're updating
)