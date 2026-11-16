
package com.example.agricultural

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agricultural.databinding.ActivityFarmBusinessBinding
import kotlinx.coroutines.launch

class FarmBusinessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFarmBusinessBinding
    private val dao by lazy { AppDatabase.get(this).farmDao() }
    private var farmId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFarmBusinessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        farmId = intent.getIntExtra("farm_id", -1)
        if (farmId <= 0) {
            Toast.makeText(
                this,
                "Invalid Farm ID",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveBusiness.setOnClickListener { saveBusiness() }

        loadBusiness()
    }

    private fun saveBusiness() {
        val crop = binding.etBusinessCropName.text.toString()
        val quantity = binding.etQuantitySold.text.toString().toDoubleOrNull()
        val price = binding.etPrice.text.toString().toDoubleOrNull()
        val expenses = binding.etExpenses.text.toString().toDoubleOrNull()

        if (crop.isBlank() || quantity == null || price == null || expenses == null ||
            quantity < 0 || price < 0 || expenses < 0) {
            Toast.makeText(this, "Enter valid information", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            dao.insertBusiness(
                FarmBusiness(
                    farmId = farmId,
                    cropName = crop,
                    quantitySold = quantity,
                    price = price,
                    expenses = expenses
                )
            )

            binding.etBusinessCropName.text.clear()
            binding.etQuantitySold.text.clear()
            binding.etPrice.text.clear()
            binding.etExpenses.text.clear()

            loadBusiness()
            Toast.makeText(this@FarmBusinessActivity, "Record saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBusiness() {
        lifecycleScope.launch {
            val records = dao.getBusiness(farmId)
            binding.layoutBusinessList.removeAllViews()

            var sales = 0.0
            var expenses = 0.0

            records.forEach { record ->
                val profit = (record.quantitySold * record.price) - record.expenses
                sales += record.quantitySold * record.price
                expenses += record.expenses

                val item = TextView(this@FarmBusinessActivity)
                item.text = "Crop: ${record.cropName}\nSold: ${record.quantitySold}\nSales: ₱${record.quantitySold * record.price}\nExpenses: ₱${record.expenses}\nProfit: ₱$profit"
                item.textSize = 16f
                binding.layoutBusinessList.addView(item)

                val delete = Button(this@FarmBusinessActivity)
                delete.text = "Delete"
                delete.setOnClickListener {
                    lifecycleScope.launch {
                        dao.deleteBusiness(record.businessId)
                        loadBusiness()
                    }
                }

                binding.layoutBusinessList.addView(delete)
            }

            binding.tvTotalSales.text = "Total Sales: ₱$sales"
            binding.tvTotalExpenses.text = "Total Expenses: ₱$expenses"
            binding.tvTotalProfit.text = "Total Profit: ₱${sales - expenses}"
        }
    }
}
