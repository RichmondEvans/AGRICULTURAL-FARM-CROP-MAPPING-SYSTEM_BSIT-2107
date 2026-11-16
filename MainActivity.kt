
package com.example.agricultural

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agricultural.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val dao by lazy {
        AppDatabase.get(this).farmDao()
    }

    private val adapter = FarmAdapter { farm ->

        val intent = Intent(
            this,
            FarmDetailsActivity::class.java
        )

        intent.putExtra("farm_id", farm.id)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupButtons()
        setupSearch()
        setupBottomNavigation()

        insertAgriculturalFarms()
    }

    private fun setupRecyclerView() {

        binding.rvFarms.layoutManager =
            LinearLayoutManager(this)

        binding.rvFarms.adapter = adapter
    }

    private fun setupButtons() {

        binding.btnAddFarm.setOnClickListener {

            startActivity(
                Intent(this, AddFarmActivity::class.java)
            )
        }
    }

    private fun setupSearch() {

        binding.etSearch.doAfterTextChanged { text ->

            val query = text?.toString()
                ?.trim()
                .orEmpty()

            loadFarms(query)
        }
    }

    private fun setupBottomNavigation() {

        binding.bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> {
                    loadFarms("")
                    true
                }

                R.id.nav_farms -> {
                    startActivity(
                        Intent(this, FarmsActivity::class.java)
                    )
                    true
                }

                R.id.nav_map -> {
                    startActivity(
                        Intent(this, FarmMapActivity::class.java)
                    )
                    true
                }

                R.id.nav_reports -> {
                    startActivity(
                        Intent(this, ReportsActivity::class.java)
                    )
                    true
                }

                else -> false
            }
        }
    }

    // AGRICULTURAL PRODUCTION FARMS

    // AGRICULTURAL PRODUCTION FARMS

    private fun insertAgriculturalFarms() {

        lifecycleScope.launch {

            try {

                val farms = listOf(

                    Farm(
                        farmName = "B2GO Farms",
                        ownerName = "Not publicly listed",
                        farmAddress = "Sitio Kaybibisaya, Brgy. Aga, Nasugbu, Batangas",
                        contactNumber = "09157473483",
                        latitude = 14.0886,
                        longitude = 120.7712
                    ),

                    Farm(
                        farmName = "3G Country Farms",
                        ownerName = "Not publicly listed",
                        farmAddress = "Sitio Kaybibisaya, Brgy. Aga, Nasugbu, Batangas",
                        contactNumber = "09669519334",
                        latitude = 14.0894,
                        longitude = 120.7725
                    ),

                    Farm(
                        farmName = "Sansar Eco Farm Inc.",
                        ownerName = "Not publicly listed",
                        farmAddress = "Brgy. Aga, Nasugbu, Batangas",
                        contactNumber = "09178739665",
                        latitude = 14.0915,
                        longitude = 120.7785
                    ),

                    Farm(
                        farmName = "Oni Agrifarm Inc.",
                        ownerName = "Not publicly listed",
                        farmAddress = "Sitio Balite, Brgy. Banilad, Nasugbu, Batangas",
                        contactNumber = "09663898830",
                        latitude = 14.0730,
                        longitude = 120.7408
                    ),

                    Farm(
                        farmName = "Sunmegaprime Pitaya Farms",
                        ownerName = "Not publicly listed",
                        farmAddress = "Brgy. Catandaan, Nasugbu, Batangas",
                        contactNumber = "09175200107",
                        latitude = 14.0804,
                        longitude = 120.6815
                    ),

                    Farm(
                        farmName = "Graceland Dragonfruit Garden",
                        ownerName = "Not publicly listed",
                        farmAddress = "Nasugbu, Batangas",
                        contactNumber = "09158980922",
                        latitude = 14.0765,
                        longitude = 120.6540
                    ),

                    Farm(
                        farmName = "3A Farm",
                        ownerName = "Not publicly listed",
                        farmAddress = "Nasugbu, Batangas",
                        contactNumber = "09175391151",
                        latitude = 14.0835,
                        longitude = 120.6680
                    ),

                    Farm(
                        farmName = "Talamante Farm",
                        ownerName = "Not publicly listed",
                        farmAddress = "Nasugbu, Batangas",
                        contactNumber = "09955102405",
                        latitude = 14.0695,
                        longitude = 120.6610
                    ),

                    Farm(
                        farmName = "TDR Farms",
                        ownerName = "Not publicly listed",
                        farmAddress = "Nasugbu, Batangas",
                        contactNumber = "",
                        latitude = 14.0750,
                        longitude = 120.6720
                    ),

                    Farm(
                        farmName = "Sissy Farm",
                        ownerName = "Not publicly listed",
                        farmAddress = "Reparo, Nasugbu, Batangas",
                        contactNumber = "09358226572",
                        latitude = 14.0728,
                        longitude = 120.6923
                    ),

                    Farm(
                        farmName = "VFNR Agricultural Farm",
                        ownerName = "Not publicly listed",
                        farmAddress = "Brgy. Tumalim, Nasugbu, Batangas",
                        contactNumber = "09174347918",
                        latitude = 14.0786,
                        longitude = 120.7224
                    )
                )

                val existingNames = dao.getAll()
                    .map {
                        it.farmName.trim().lowercase()
                    }
                    .toMutableSet()

                var addedCount = 0

                for (farm in farms) {

                    val name = farm.farmName
                        .trim()
                        .lowercase()

                    if (name !in existingNames) {

                        dao.insert(farm)

                        existingNames.add(name)

                        addedCount++
                    }
                }

                loadFarms("")

                if (addedCount > 0) {
                    Toast.makeText(
                        this@MainActivity,
                        "$addedCount agricultural farm records added!",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@MainActivity,
                    "Database error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    // LOAD FARMS

    private fun loadFarms(query: String) {

        lifecycleScope.launch {

            try {

                val farms = if (query.isBlank()) {
                    dao.getAll()
                } else {
                    dao.search(query)
                }

                adapter.submitList(farms)

                binding.tvFarmCount.text =
                    "${farms.size} Farms"

            } catch (e: Exception) {

                Toast.makeText(
                    this@MainActivity,
                    "Unable to load farms: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {

        super.onResume()

        if (::binding.isInitialized) {

            loadFarms(
                binding.etSearch.text
                    ?.toString()
                    ?.trim()
                    .orEmpty()
            )
        }
    }
}
