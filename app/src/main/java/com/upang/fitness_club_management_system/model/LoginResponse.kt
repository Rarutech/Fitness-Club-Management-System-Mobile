package com.upang.fitness_club_management_system.model

data class LoginResponse(
    val message: String,
    val token: String? = null,
    val email: String,
    val role: String
)
