
package com.example.agricultural

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agricultural.databinding.ActivityProductionAreaBinding
import kotlinx.coroutines.launch

class ProductionAreaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductionAreaBinding
    private val dao by lazy { AppDatabase.get(this).farmDao() }
    private var farmId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductionAreaBinding.inflate(layoutInflater)
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
        binding.btnSaveArea.setOnClickListener { saveArea() }

        loadAreas()
    }

    private fun saveArea() {
        val unit = binding.etAreaUnit.text.toString()
        val soil = binding.etSoilType.text.toString()
        val status = binding.etLandStatus.text.toString()

        if (unit.isBlank() || soil.isBlank() || status.isBlank()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            dao.insertArea(
                ProductionArea(
                    farmId = farmId,
                    areaUnit = unit,
                    soilType = soil,
                    currentLandStatus = status
                )
            )

            binding.etAreaUnit.text.clear()
            binding.etSoilType.text.clear()
            binding.etLandStatus.text.clear()

            loadAreas()
            Toast.makeText(this@ProductionAreaActivity, "Area saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAreas() {
        lifecycleScope.launch {
            val areas = dao.getAreas(farmId)
            binding.layoutAreaList.removeAllViews()

            areas.forEach { area ->
                val item = TextView(this@ProductionAreaActivity)
                item.text = "Area: ${area.areaUnit}\nSoil: ${area.soilType}\nStatus: ${area.currentLandStatus}"
                item.textSize = 16f
                binding.layoutAreaList.addView(item)

                val delete = Button(this@ProductionAreaActivity)
                delete.text = "Delete"
                delete.setOnClickListener {
                    lifecycleScope.launch {
                        dao.deleteArea(area.areaId)
                        loadAreas()
                    }
                }

                binding.layoutAreaList.addView(delete)
            }
        }
    }
}
