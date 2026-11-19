package com.example.agricultural

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

// Agricultural Reports screen: totals, then a summary per farm.
class ReportsActivity : BaseActivity() {
    private val reportService by lazy {
        ReportService(
            farms = database.farmDao(),
            crops = database.cropDao(),
            areas = database.productionAreaDao(),
            harvests = database.harvestDao(),
            business = database.businessDao()
        )
    }

    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        content.addView(text("Agricultural Reports", 26f, bold = true))
        content.addView(text("Loading report...", 16f))

        setContentView(ScrollView(this).apply { addView(content) })

        loadReport()
    }

    private fun loadReport() {
        launchSafely("Unable to load reports") {
            show(reportService.generate())
        }
    }

    private fun show(report: AgriculturalReport) {
        content.removeAllViews()

        content.addView(text("Agricultural Reports", 26f, bold = true))

        content.addView(text("Registered Farms: ${report.farmCount}", 18f))
        content.addView(text("Crops: ${report.cropCount}", 18f))
        content.addView(text("Production Areas: ${report.areaCount}", 18f))
        content.addView(
            text("Total Available Harvest: ${report.totalHarvestQuantity}", 18f)
        )
        content.addView(
            text("Total Sales: ${Money.format(report.finance.totalSales)}", 18f)
        )
        content.addView(
            text("Total Expenses: ${Money.format(report.finance.totalExpenses)}", 18f)
        )
        content.addView(
            text(
                "Total Profit: ${Money.format(report.finance.profit)}",
                18f,
                bold = true,
                color = if (report.finance.profit >= 0) CardLine.POSITIVE else Color.RED
            )
        )

        content.addView(text("Per Farm", 20f, bold = true, top = 28))

        if (report.farms.isEmpty()) {
            content.addView(text("No registered farms yet.", 15f, color = Color.GRAY))
        }

        report.farms.forEach { content.addView(farmRow(it)) }

        content.addView(Button(this).apply {
            text = "Back"
            setOnClickListener { finish() }
        })
    }

    private fun farmRow(item: FarmReport) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(20, 16, 20, 16)
        setBackgroundColor(Color.rgb(240, 248, 240))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 8, 0, 8) }

        addView(text(item.farm.farmName, 17f, bold = true, color = CardLine.GREEN))
        addView(
            text(
                "Crops: ${item.cropCount} · Areas: ${item.areaCount} · " +
                    "Harvest: ${item.totalHarvestQuantity}",
                14f,
                top = 4
            )
        )
        addView(
            text(
                "Profit: ${Money.format(item.finance.profit)}",
                14f,
                top = 2,
                color = if (item.finance.profit >= 0) CardLine.POSITIVE else Color.RED
            )
        )
    }

    private fun text(
        value: String,
        size: Float,
        bold: Boolean = false,
        color: Int = Color.DKGRAY,
        top: Int = 8
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
        setPadding(0, top, 0, 0)
    }
}
