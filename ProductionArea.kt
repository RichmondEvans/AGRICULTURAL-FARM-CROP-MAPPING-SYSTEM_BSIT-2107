package com.example.agricultural

data class ProductionArea(
    override val id: Int = 0,
    override val farmId: Int,
    val areaUnit: String,
    val soilType: String,
    val currentLandStatus: String
) : AgriculturalRecord() {

    override val displayTitle: String
        get() = "Production Area"
}
