package com.example.agricultural


data class Harvest(
    override val id: Int = 0,
    override val farmId: Int,
    val cropId: Int,
    val harvestDate: String,
    val harvestQuality: String,
    val availableQuantity: Double
) : AgriculturalRecord() {

    override val displayTitle: String
        get() = "Harvest Record"
}
