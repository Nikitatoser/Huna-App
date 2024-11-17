package com.example.huna_app

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val address: String,

    val addDate: String,
    val available: Boolean,
    val category: String,
    val imageUrl: String
)
