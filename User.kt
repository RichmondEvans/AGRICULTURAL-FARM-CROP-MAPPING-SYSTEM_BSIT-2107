package com.example.agricultural

data class User(
    val id: Int,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val createdAt: String
)