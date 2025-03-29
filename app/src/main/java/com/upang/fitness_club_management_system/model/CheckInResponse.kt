package com.upang.fitness_club_management_system.model

data class CheckInResponse(
    val success: Boolean,
    val message: String,
    val id: Int,
    val checkin_date: String,
    val checkin_time: String
)
