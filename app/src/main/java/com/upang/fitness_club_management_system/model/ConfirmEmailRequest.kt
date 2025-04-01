package com.upang.fitness_club_management_system.model

data class ConfirmEmailRequest(
    val email: String,
    val code: String
)
