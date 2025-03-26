package com.upang.fitness_club_management_system.model

data class BookTrainerRequest(
    val user_email: String,
    val trainer_email: String,
    val date_of_training: String,
    val time_start: String,
    val time_end: String,
    val description: String

)
