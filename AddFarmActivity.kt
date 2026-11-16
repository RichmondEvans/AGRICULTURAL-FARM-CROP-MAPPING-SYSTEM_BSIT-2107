package com.example.agricultural

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agricultural.databinding.ActivityAddFarmBinding
import kotlinx.coroutines.launch

class AddFarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveFarm() }
    }

    private fun saveFarm() {
        val name = binding.etFarmName.text.toString().trim()
        val owner = binding.etOwnerName.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val contact = binding.etContact.text.toString().trim()
        val lat = binding.etLatitude.text.toString().trim().toDoubleOrNull()
        val lng = binding.etLongitude.text.toString().trim().toDoubleOrNull()

        if (name.isEmpty() || owner.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Please fill in all farm details", Toast.LENGTH_SHORT).show()
            return
        }
        if (lat == null || lng == null) {
            Toast.makeText(this, "Enter a valid latitude and longitude", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            AppDatabase.get(this@AddFarmActivity).farmDao()
                .insert(Farm(farmName = name, ownerName = owner, farmAddress = address,
                    contactNumber = contact, latitude = lat, longitude = lng))
            Toast.makeText(this@AddFarmActivity, "Farm saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}