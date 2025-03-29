package com.upang.fitness_club_management_system.model

data class CheckOutResponse(
    val success: Boolean,
    val message: String,
    val checkout_time: String
)
