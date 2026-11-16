package com.example.agricultural

data class ProductionArea(
    val areaId: Int = 0,
    val farmId: Int,
    val areaUnit: String,
    val soilType: String,
    val currentLandStatus: String
)