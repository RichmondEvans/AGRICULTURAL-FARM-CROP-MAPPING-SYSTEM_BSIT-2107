package com.example.agricultural
data class CropBoundary(
    val cropId: Int,
    val cropName: String,
    val points: List<Pair<Double, Double>>
)
