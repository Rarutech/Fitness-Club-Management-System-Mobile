package com.upang.fitness_club_management_system.model

data class TrainerFetchData(
    val request_id: Int,
    val user_email: String,
    val trainer_name: String,
    val user_name: String,
    val request_date: String,
    val status: String,
    val date_of_training: String,
    val time_start: String,
    val time_end: String,
    val description: String,
    val trainer_email: String
)

// Wrapper for the API response
data class TrainerFetchApiResponse(
    val status: String,
    val message: String,
    val request: TrainerFetchData
)
