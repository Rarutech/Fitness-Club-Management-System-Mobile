package com.upang.fitness_club_management_system.model

data class Users(
    val id: Int,
    val fullname: String,
    val password: String,
    val role: String
)
