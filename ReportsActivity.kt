package com.example.agricultural

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ReportsActivity : AppCompatActivity() {

    private val dao by lazy {
        AppDatabase.get(this).farmDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Agricultural Reports"
            textSize = 26f
        }

        val farmCount = TextView(this).apply {
            text = "Loading farms..."
            textSize = 18f
        }

        val profit = TextView(this).apply {
            text = "Loading profit..."
            textSize = 18f
        }

        val backButton = Button(this).apply {
            text = "Back"
            setOnClickListener { finish() }
        }

        layout.addView(title)
        layout.addView(farmCount)
        layout.addView(profit)
        layout.addView(backButton)

        setContentView(layout)

        lifecycleScope.launch {
            try {
                val farms = dao.count()
                val totalProfit = dao.getTotalProfit()

                farmCount.text = "Registered Farms: $farms"
                profit.text = "Total Profit: ₱${"%.2f".format(totalProfit)}"

            } catch (e: Exception) {
                farmCount.text = "Unable to load reports"
                profit.text = e.message ?: "Unknown error"
            }
        }
    }
}