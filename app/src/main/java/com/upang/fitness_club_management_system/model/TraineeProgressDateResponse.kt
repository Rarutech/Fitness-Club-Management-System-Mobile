package com.upang.fitness_club_management_system.model

data class TraineeProgressDateResponse(
    val success: Boolean,
    val checkin_time: String,
    val checkout_time: String
)
