package com.upang.fitness_club_management_system.model

data class UpdateTrainerProfileRequest(
    var email: String,
    var fullname: String,
    var about_text: String
)
