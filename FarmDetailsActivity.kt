package com.example.agricultural

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agricultural.databinding.ActivityFarmDetailsBinding
import kotlinx.coroutines.launch
import android.widget.Toast

class FarmDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFarmDetailsBinding

    private var farmId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFarmDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        farmId = intent.getIntExtra("farm_id", -1)

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Load Farm Information
        loadFarmDetails()

        // 1. CROPS INFORMATION
        binding.cardCrops.setOnClickListener {
            openModule(CropsActivity::class.java)
        }

        // 2. PRODUCTION AREAS
        binding.cardAreas.setOnClickListener {
            openModule(ProductionAreaActivity::class.java)
        }

        // 3. HARVEST RECORDS
        binding.cardHarvest.setOnClickListener {
            openModule(HarvestActivity::class.java)
        }

        // 4. FARM BUSINESS
        binding.cardBusiness.setOnClickListener {
            openModule(FarmBusinessActivity::class.java)
        }
    }

    // OPEN FEATURES
    private fun openModule(activity: Class<*>) {

        if (farmId <= 0) {
            Toast.makeText(
                this,
                "Invalid Farm ID",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            val moduleIntent = Intent(this, activity)
            moduleIntent.putExtra("farm_id", farmId)
            startActivity(moduleIntent)

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open module: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // LOAD FARM DETAILS
    private fun loadFarmDetails() {

        lifecycleScope.launch {

            val farm = AppDatabase.get(this@FarmDetailsActivity)
                .farmDao()
                .getById(farmId)

            if (farm == null) {
                finish()
                return@launch
            }

            binding.tvFarmName.text = farm.farmName
            binding.tvFarmId.text = "Farm ID: ${farm.farmCode}"
            binding.tvOwner.text = farm.ownerName
            binding.tvContact.text = farm.contactNumber
            binding.tvAddress.text = farm.farmAddress
            binding.tvLatitude.text = "Latitude: ${farm.latitude}"
            binding.tvLongitude.text = "Longitude: ${farm.longitude}"
        }
    }
}
