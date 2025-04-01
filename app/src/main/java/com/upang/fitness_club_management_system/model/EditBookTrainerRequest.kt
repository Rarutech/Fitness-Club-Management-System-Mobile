package com.upang.fitness_club_management_system.model

data class EditBookTrainerRequest(
    val request_id: Int,
    val date_of_training: String,
    val time_start: String,
    val time_end: String,
    val description: String
)
