
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
    private val database by lazy {
        AppDatabase.get(this)
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

            val query = text?.toString()?.trim().orEmpty()

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

                R.id.nav_profile -> {
                    startActivity(
                        Intent(this, ProfileActivity::class.java)
                    )
                    true
                }

                else -> false
            }
        }
    }

    private fun insertAgriculturalFarms() {

        lifecycleScope.launch {

            try {

                val seeder = SampleDataSeeder(
                    farmRepository = dao,
                    cropRepository = database.cropDao(),
                    harvestRepository = database.harvestDao(),
                    areaRepository = database.productionAreaDao()
                )

                val addedCount = seeder.seed()

                loadFarms("")

                if (addedCount > 0) {
                    Toast.makeText(
                        this@MainActivity,
                        "$addedCount new farm records added!",
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
                    "Search error: ${e.message}",
                    Toast.LENGTH_LONG
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
