package com.example.agricultural

/**
 * A harvest belongs to a crop. [farmId] is not stored in the
 * harvests table; it is read from the crop through a join so the
 * harvest can still be treated as a farm record.
 */
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
