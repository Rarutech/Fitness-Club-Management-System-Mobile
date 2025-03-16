package com.upang.fitness_club_management_system.model

data class OrderRequest(
    val email: String,
    val product_name: String,
    val quantity: Int
)
