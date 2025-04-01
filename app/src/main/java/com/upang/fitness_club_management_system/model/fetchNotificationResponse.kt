package com.upang.fitness_club_management_system.model

data class fetchNotificationResponse(
    val status: String,
    val notifications: List<Notification>
)

data class Notification(
    val id: Int,
    val message: String,
    val created_at: String
)
