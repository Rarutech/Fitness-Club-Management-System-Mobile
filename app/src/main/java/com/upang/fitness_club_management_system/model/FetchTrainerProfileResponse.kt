package com.upang.fitness_club_management_system.model

data class FetchTrainerProfileResponse(
    val status: String,
    val message: String,
    val fullname: String,
    val email: String,
    val role: String,
    val profile_picture: String,
    val about: String
)