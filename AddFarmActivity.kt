
package com.example.agricultural

import android.net.Uri
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agricultural.databinding.ActivityAddFarmBinding
import kotlinx.coroutines.launch

class AddFarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFarmBinding

    private var selectedFarmPhoto: Uri? = null

    // =====================================
    // PHOTO PICKER
    // =====================================

    private val photoPicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                try {

                    // Save permanent URI read permission
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    selectedFarmPhoto = uri

                    // Display photo preview
                    binding.imgFarmPhoto.setImageURI(uri)

                    Toast.makeText(
                        this,
                        "Farm photo selected successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (e: Exception) {

                    selectedFarmPhoto = null

                    Toast.makeText(
                        this,
                        "Unable to access photo: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    // =====================================
    // ON CREATE
    // =====================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddFarmBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Choose farm photo
        binding.btnChoosePhoto.setOnClickListener {
            photoPicker.launch(arrayOf("image/*"))
        }

        // Save farm
        binding.btnSave.setOnClickListener {
            saveFarm()
        }
    }

    // =====================================
    // SAVE FARM
    // =====================================

    private fun saveFarm() {

        val name = binding.etFarmName.text.toString().trim()
        val owner = binding.etOwnerName.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val contact = binding.etContact.text.toString().trim()

        val lat = binding.etLatitude.text.toString()
            .trim().toDoubleOrNull()

        val lng = binding.etLongitude.text.toString()
            .trim().toDoubleOrNull()

        // Validate farm details
        if (
            name.isEmpty() ||
            owner.isEmpty() ||
            address.isEmpty() ||
            contact.isEmpty()
        ) {

            Toast.makeText(
                this,
                "Please fill in all farm details",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // Validate coordinates
        if (
            lat == null ||
            lng == null ||
            lat !in -90.0..90.0 ||
            lng !in -180.0..180.0
        ) {

            Toast.makeText(
                this,
                "Enter valid latitude and longitude",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // Save farm and photo URI
        lifecycleScope.launch {

            try {

                val farm = Farm(
                    farmName = name,
                    ownerName = owner,
                    farmAddress = address,
                    contactNumber = contact,
                    latitude = lat,
                    longitude = lng,
                    farmPhoto = selectedFarmPhoto?.toString() ?: ""
                )

                AppDatabase.get(this@AddFarmActivity)
                    .farmDao()
                    .insert(farm)

                Toast.makeText(
                    this@AddFarmActivity,
                    "Farm saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

            } catch (e: Exception) {

                Toast.makeText(
                    this@AddFarmActivity,
                    "Unable to save farm: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
