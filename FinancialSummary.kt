package com.example.agricultural


data class FinancialSummary(
    val totalSales: Double,
    val totalExpenses: Double
) {
    val profit: Double
        get() = totalSales - totalExpenses

    operator fun plus(other: FinancialSummary) = FinancialSummary(
        totalSales + other.totalSales,
        totalExpenses + other.totalExpenses
    )

    companion object {
        val EMPTY = FinancialSummary(0.0, 0.0)

        fun of(records: List<FarmBusiness>) = FinancialSummary(
            totalSales = records.sumOf { it.totalSales },
            totalExpenses = records.sumOf { it.expenses }
        )
    }
}
