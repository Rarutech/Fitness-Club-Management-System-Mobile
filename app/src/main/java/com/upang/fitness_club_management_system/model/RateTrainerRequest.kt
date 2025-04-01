package com.upang.fitness_club_management_system.model

data class RateTrainerRequest(
    val user_name: String,
    val user_email: String,
    val trainer_name: String,
    val trainer_email: String,
    val rating: Double,
    val comment: String
)
