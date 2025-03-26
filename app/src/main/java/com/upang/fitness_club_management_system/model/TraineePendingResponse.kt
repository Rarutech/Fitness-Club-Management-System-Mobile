package com.upang.fitness_club_management_system.model

data class TraineePendingResponse(
    val status: String,
    val message: String,
    val request_id: Int,
    val new_status: String
)
