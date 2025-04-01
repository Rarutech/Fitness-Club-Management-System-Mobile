package com.upang.fitness_club_management_system.model

data class TraineeProgressResponse(
    val success: Boolean,
    val fullname: String,
    val total_days: Int,
    val total_workout_hours: Int,
    val this_week_days: Int,
    val this_month_days: Int
)
