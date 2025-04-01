package com.upang.fitness_club_management_system.model

data class TrainerReviewResponse(
    val success: Boolean,
    val reviews: List<TrainerReview>
)
data class TrainerReview(
    val id: Int,
    val user_name: String,
    val trainer_name: String,
    val trainer_email: String,
    val rating: Float,
    val comment: String,
    val created_at: String,
    val user_picture: String,
    val user_email: String,
    val average_rating: Float
)
