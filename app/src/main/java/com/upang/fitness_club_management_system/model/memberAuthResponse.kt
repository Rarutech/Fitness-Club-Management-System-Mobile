package com.upang.fitness_club_management_system.model

data class memberAuthResponse(
    val success: Boolean,
    val message: String,
    val is_active: Boolean
)
