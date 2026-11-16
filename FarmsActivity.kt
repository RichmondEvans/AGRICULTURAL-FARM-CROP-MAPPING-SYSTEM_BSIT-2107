
package com.example.agricultural

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agricultural.databinding.ActivityFarmsBinding
import kotlinx.coroutines.launch

class FarmsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFarmsBinding

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

        binding = ActivityFarmsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvFarms.layoutManager =
            LinearLayoutManager(this)

        binding.rvFarms.adapter = adapter

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.etSearch.doAfterTextChanged { text ->
            loadFarms(text?.toString()?.trim().orEmpty())
        }

        loadFarms("")
    }

    private fun loadFarms(query: String) {

        lifecycleScope.launch {

            val farms = if (query.isBlank()) {
                dao.getAll()
            } else {
                dao.search(query)
            }

            adapter.submitList(farms)

            binding.tvFarmCount.text =
                "${farms.size} Registered Farms"
        }
    }
}
