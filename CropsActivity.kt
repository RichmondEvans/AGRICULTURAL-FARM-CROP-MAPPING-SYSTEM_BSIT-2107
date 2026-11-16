
package com.example.agricultural

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agricultural.databinding.ActivityCropsBinding
import kotlinx.coroutines.launch

class CropsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropsBinding

    private val dao by lazy {
        AppDatabase.get(this).farmDao()
    }

    private var farmId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCropsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        farmId = intent.getIntExtra("farm_id", -1)

        if (farmId == -1) {
            Toast.makeText(
                this,
                "Invalid Farm ID",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSaveCrop.setOnClickListener {
            saveCrop()
        }

        loadCrops()
    }

    private fun saveCrop() {
        val name = binding.etCropName.text.toString().trim()
        val variety = binding.etCropVariety.text.toString().trim()
        val status = binding.etCropStatus.text.toString().trim()

        if (name.isBlank() || variety.isBlank() || status.isBlank()) {
            Toast.makeText(
                this,
                "Please fill all fields",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {
            try {
                dao.insertCrop(
                    Crop(
                        farmId = farmId,
                        cropName = name,
                        cropVariety = variety,
                        cropStatus = status
                    )
                )

                binding.etCropName.text.clear()
                binding.etCropVariety.text.clear()
                binding.etCropStatus.text.clear()

                loadCrops()

                Toast.makeText(
                    this@CropsActivity,
                    "Crop saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@CropsActivity,
                    "Save failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadCrops() {
        lifecycleScope.launch {
            try {
                val crops = dao.getCrops(farmId)

                binding.layoutCropList.removeAllViews()

                crops.forEach { crop ->

                    val item = TextView(this@CropsActivity)
                    item.text =
                        "${crop.cropName} - ${crop.cropVariety} - ${crop.cropStatus}"
                    item.textSize = 16f
                    item.setPadding(8, 12, 8, 12)

                    binding.layoutCropList.addView(item)

                    val delete = Button(this@CropsActivity)
                    delete.text = "Delete"

                    delete.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                dao.deleteCrop(crop.cropId)
                                loadCrops()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@CropsActivity,
                                    "Delete failed: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    binding.layoutCropList.addView(delete)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@CropsActivity,
                    "Loading failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
