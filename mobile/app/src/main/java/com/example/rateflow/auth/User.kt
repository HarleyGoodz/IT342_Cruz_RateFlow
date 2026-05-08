package com.example.rateflow.auth

data class User(
    val id: Int? = null,
    val username: String? = null,
    val email: String? = null,
    val role: String? = null,
    val password: String? = null  // This might be null for security
)