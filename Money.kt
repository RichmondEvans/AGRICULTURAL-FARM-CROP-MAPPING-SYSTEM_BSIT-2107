package com.example.agricultural

import java.util.Locale

// Peso Sign formatting
object Money {
    fun format(amount: Double): String =
        "₱" + String.format(Locale.US, "%,.2f", amount)
}
