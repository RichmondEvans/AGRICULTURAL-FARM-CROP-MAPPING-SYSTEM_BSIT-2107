package com.example.agricultural

data class Farm(
    override val id: Int = 0,
    val farmName: String,
    val ownerName: String,
    val farmAddress: String,
    val contactNumber: String,
    val latitude: Double,
    val longitude: Double,
    val farmPhoto: String = "",
    val website: String = ""
) : AgriculturalRecord() {

    override val farmId: Int
        get() = id

    override val displayTitle: String
        get() = farmName

    val farmCode: String
        get() = "F${142 + id}"
}
