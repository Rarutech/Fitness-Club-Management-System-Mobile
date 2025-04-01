package com.upang.fitness_club_management_system.model

data class FetchTrainersResponse(
    val status: String,
    val message: String,
    val profiles: List<Profile>
)

data class Profile(
    val fullname: String,
    val email: String,
    val role: String,
    val profile_picture: String,
    val about: String,
    val total_ratings: Double
)