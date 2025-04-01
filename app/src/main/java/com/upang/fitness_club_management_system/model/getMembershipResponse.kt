package com.upang.fitness_club_management_system.model

data class getMembershipResponse(
    val success: Boolean,
    val message: String,
    val membership_start: String,
    val membership_end: String,
    val next_payment_date: String,
    val status: String
)
