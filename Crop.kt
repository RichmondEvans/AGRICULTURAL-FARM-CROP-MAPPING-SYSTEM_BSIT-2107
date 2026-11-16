package com.example.agricultural

data class Crop(
    val cropId: Int = 0,
    val farmId: Int,
    val cropName: String,
    val cropVariety: String,
    val cropStatus: String
)