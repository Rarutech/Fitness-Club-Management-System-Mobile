package com.upang.fitness_club_management_system.model

data class FetchInventoryResponse(
    val status: String,
    val data: List<Product> // ✅ Now correctly represents an array of products
)

data class Product(
    val id: Int,
    val product_name: String,
    val description: String,
    val price: String,
    val stock_quantity: Int,
    val created_at: String,
    val product_image: String
)
