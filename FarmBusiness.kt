package com.example.agricultural

data class FarmBusiness(
    val businessId: Int = 0,
    val farmId: Int,
    val cropName: String,
    val quantitySold: Double,
    val price: Double,
    val expenses: Double
) {
    val totalSales: Double
        get() = quantitySold * price

    val profit: Double
        get() = totalSales - expenses
}