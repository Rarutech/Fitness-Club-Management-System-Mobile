package com.upang.fitness_club_management_system.model

data class PaymentIntentResponse(
    val clientSecret: String,
    val error: String?
)

