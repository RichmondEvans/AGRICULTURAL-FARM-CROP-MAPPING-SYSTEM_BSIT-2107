package com.example.agricultural

data class Crop(
    override val id: Int = 0,
    override val farmId: Int,
    val cropName: String,
    val cropVariety: String,
    val cropStatus: String
) : AgriculturalRecord() {

    override val displayTitle: String
        get() = cropName
}
