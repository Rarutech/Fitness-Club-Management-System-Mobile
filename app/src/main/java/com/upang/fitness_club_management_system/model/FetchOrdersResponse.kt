package com.upang.fitness_club_management_system.model

data class FetchOrdersResponse(
    val status: String,
    val message: String,
    val fullname: String,
    val orders: List<Order>
)

data class Order(
    val id: Int,
    val customer_name: String,
    val order_date: String,
    val product_name: String,
    val product_picture: String,
    val price: String,
    val quantity: Int,
    val status: String
)