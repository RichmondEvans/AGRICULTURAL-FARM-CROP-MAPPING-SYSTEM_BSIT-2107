package com.example.agricultural

data class Harvest(
    val harvestId: Int = 0,
    val cropId: Int,
    val harvestDate: String,
    val harvestQuality: String,
    val availableQuantity: Double
)