package com.example.agricultural

data class FarmBusiness(
    override val id: Int = 0,
    override val farmId: Int,
    val cropName: String,
    val quantitySold: Double,
    val price: Double,
    val expenses: Double
) : AgriculturalRecord() {

    override val displayTitle: String
        get() = cropName

    val totalSales: Double
        get() = quantitySold * price

    val profit: Double
        get() = totalSales - expenses
}
