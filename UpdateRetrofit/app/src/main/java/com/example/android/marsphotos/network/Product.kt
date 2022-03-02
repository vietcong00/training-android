package com.example.android.marsphotos.network

data class Product(
    val id: Int,
    val name: String,
    val descriptions: String,
    val category: Category,

    )

data class Category(
    val id: Number,
    val name: String,
)
