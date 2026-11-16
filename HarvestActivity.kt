
package com.example.agricultural

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agricultural.databinding.ActivityHarvestBinding
import kotlinx.coroutines.launch

class HarvestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHarvestBinding
    private val dao by lazy { AppDatabase.get(this).farmDao() }
    private var farmId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHarvestBinding.inflate(layoutInflater)
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
        binding.btnSaveHarvest.setOnClickListener { saveHarvest() }

        loadHarvests()
    }

    private fun saveHarvest() {
        val cropId = binding.etCropId.text.toString().toIntOrNull()
        val date = binding.etHarvestDate.text.toString()
        val quality = binding.etHarvestQuality.text.toString()
        val quantity = binding.etAvailableQuantity.text.toString().toDoubleOrNull()

        if (cropId == null || date.isBlank() || quality.isBlank() || quantity == null || quantity < 0) {
            Toast.makeText(this, "Enter valid information", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val crops = dao.getCrops(farmId)

            if (crops.none { it.cropId == cropId }) {
                Toast.makeText(this@HarvestActivity, "Invalid Crop ID", Toast.LENGTH_SHORT).show()
                return@launch
            }

            dao.insertHarvest(
                Harvest(
                    cropId = cropId,
                    harvestDate = date,
                    harvestQuality = quality,
                    availableQuantity = quantity
                )
            )

            binding.etCropId.text.clear()
            binding.etHarvestDate.text.clear()
            binding.etHarvestQuality.text.clear()
            binding.etAvailableQuantity.text.clear()

            loadHarvests()
            Toast.makeText(this@HarvestActivity, "Harvest saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadHarvests() {
        lifecycleScope.launch {
            val harvests = dao.getHarvests(farmId)
            binding.layoutHarvestList.removeAllViews()

            harvests.forEach { harvest ->
                val item = TextView(this@HarvestActivity)
                item.text = "Crop ID: ${harvest.cropId}\nDate: ${harvest.harvestDate}\nQuality: ${harvest.harvestQuality}\nQuantity: ${harvest.availableQuantity}"
                item.textSize = 16f
                binding.layoutHarvestList.addView(item)

                val delete = Button(this@HarvestActivity)
                delete.text = "Delete"
                delete.setOnClickListener {
                    lifecycleScope.launch {
                        dao.deleteHarvest(harvest.harvestId)
                        loadHarvests()
                    }
                }

                binding.layoutHarvestList.addView(delete)
            }
        }
    }
}
