package com.example.agricultural

import android.graphics.Color
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import com.example.agricultural.databinding.ActivityFarmBusinessBinding

/** Farm Business screen: sales, expenses and profit. */
class FarmBusinessActivity :
    RecordCrudActivity<FarmBusiness, ActivityFarmBusinessBinding>() {

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityFarmBusinessBinding.inflate(inflater)

    override val repository: BusinessRepository by lazy { database.businessDao() }

    override val backButton: Button get() = binding.btnBack
    override val saveButton: Button get() = binding.btnSaveBusiness
    override val listContainer: LinearLayout get() = binding.layoutBusinessList

    override val addLabel = "＋ Save Business Record"
    override val updateLabel = "Update Record"
    override val editLabel = "Edit Record"
    override val deleteLabel = "Delete Record"
    override val emptyMessage = "No business records available."
    override val savedMessage = "Business record saved!"
    override val deletedMessage = "Business record deleted"

    override fun readForm(recordId: Int): FarmBusiness? {
        val crop = binding.etBusinessCropName.text.toString().trim()
        val quantity = binding.etQuantitySold.text.toString().trim().toDoubleOrNull()
        val price = binding.etPrice.text.toString().trim().toDoubleOrNull()
        val expenses = binding.etExpenses.text.toString().trim().toDoubleOrNull()

        if (
            crop.isBlank() ||
            quantity == null || quantity < 0 ||
            price == null || price < 0 ||
            expenses == null || expenses < 0
        ) {
            toast("Please enter valid information")
            return null
        }

        return FarmBusiness(
            id = recordId,
            farmId = farmId,
            cropName = crop,
            quantitySold = quantity,
            price = price,
            expenses = expenses
        )
    }

    override fun fillForm(record: FarmBusiness) {
        binding.etBusinessCropName.setText(record.cropName)
        binding.etQuantitySold.setText(record.quantitySold.toString())
        binding.etPrice.setText(record.price.toString())
        binding.etExpenses.setText(record.expenses.toString())
    }

    override fun clearForm() {
        binding.etBusinessCropName.text.clear()
        binding.etQuantitySold.text.clear()
        binding.etPrice.text.clear()
        binding.etExpenses.text.clear()
    }

    override fun cardLines(record: FarmBusiness) = listOf(
        CardLine.normal("Quantity Sold: ${record.quantitySold}"),
        CardLine.normal("Price per Unit: ${Money.format(record.price)}"),
        CardLine.normal("Sales: ${Money.format(record.totalSales)}"),
        CardLine.normal("Expenses: ${Money.format(record.expenses)}"),
        CardLine.signed(
            "Profit: ${Money.format(record.profit)}",
            positive = record.profit >= 0
        )
    )

    // Totals shown at the top of the screen
    override fun onRecordsLoaded(records: List<FarmBusiness>) {
        val summary = FinancialSummary.of(records)

        binding.tvTotalSales.text =
            "Total Sales: ${Money.format(summary.totalSales)}"

        binding.tvTotalExpenses.text =
            "Total Expenses: ${Money.format(summary.totalExpenses)}"

        binding.tvTotalProfit.text =
            "Total Profit: ${Money.format(summary.profit)}"

        binding.tvTotalProfit.setTextColor(
            if (summary.profit >= 0) CardLine.POSITIVE else Color.RED
        )
    }
}
