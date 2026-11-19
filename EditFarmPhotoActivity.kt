
package com.example.agricultural

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class EditFarmPhotoActivity : AppCompatActivity() {

    private lateinit var spFarm: Spinner
    private lateinit var imgFarm: ImageView
    private lateinit var btnChoose: Button
    private lateinit var btnSave: Button

    private var farms: List<Farm> = emptyList()
    private var selectedFarm: Farm? = null
    private var selectedPhoto: Uri? = null

    private val picker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    selectedPhoto = uri
                    imgFarm.setImageURI(uri)

                } catch (_: Exception) {
                    Toast.makeText(
                        this,
                        "Unable to access photo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_farm_photo)

        spFarm = findViewById(R.id.spFarm)
        imgFarm = findViewById(R.id.imgFarm)
        btnChoose = findViewById(R.id.btnChoosePhoto)
        btnSave = findViewById(R.id.btnSavePhoto)

        loadFarms()

        btnChoose.setOnClickListener {
            picker.launch(arrayOf("image/*"))
        }

        btnSave.setOnClickListener {
            savePhoto()
        }
    }

    private fun loadFarms() {
        lifecycleScope.launch {
            try {
                farms = AppDatabase.get(this@EditFarmPhotoActivity)
                    .farmDao()
                    .getAll()

                if (farms.isEmpty()) {
                    Toast.makeText(
                        this@EditFarmPhotoActivity,
                        "No farms found",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val adapter = ArrayAdapter(
                    this@EditFarmPhotoActivity,
                    android.R.layout.simple_spinner_item,
                    farms.map { it.farmName }
                )

                adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )

                spFarm.adapter = adapter

                spFarm.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: android.view.View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedFarm = farms[position]
                            selectedPhoto = null

                            val photo = farms[position].farmPhoto

                            if (photo.isNotBlank()) {
                                imgFarm.setImageURI(Uri.parse(photo))
                            } else {
                                imgFarm.setImageResource(
                                    R.drawable.farm_placeholder
                                )
                            }
                        }

                        override fun onNothingSelected(
                            parent: AdapterView<*>?
                        ) {}
                    }

            } catch (e: Exception) {
                Toast.makeText(
                    this@EditFarmPhotoActivity,
                    "Unable to load farms: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun savePhoto() {

        val farm = selectedFarm
        val photo = selectedPhoto

        if (farm == null || photo == null) {
            Toast.makeText(
                this,
                "Select a farm and a photo first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {
            try {
                AppDatabase.get(this@EditFarmPhotoActivity)
                    .farmDao()
                    .updateFarmPhoto(
                        farm.id,
                        photo.toString()
                    )

                Toast.makeText(
                    this@EditFarmPhotoActivity,
                    "Farm photo updated successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@EditFarmPhotoActivity,
                    "Unable to update photo: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
