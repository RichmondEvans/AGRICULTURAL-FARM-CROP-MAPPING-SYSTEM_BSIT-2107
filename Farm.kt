package com.example.agricultural

data class Farm(
    val id: Int = 0,
    val farmName: String,
    val ownerName: String,
    val farmAddress: String,
    val contactNumber: String,
    val latitude: Double,
    val longitude: Double
) {
    val farmCode: String get() = "F" + (142 + id)
}