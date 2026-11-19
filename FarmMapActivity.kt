
package com.example.agricultural

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.CopyrightOverlay
import java.io.File
import java.util.Locale
import androidx.activity.result.contract.ActivityResultContracts

class FarmMapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var spBarangay: Spinner

    private val dao by lazy {
        AppDatabase.get(this).farmDao()
    }

    // =====================================
    // BARANGAY LOCATIONS
    // =====================================

    private val barangayLocations = linkedMapOf(

        "All Barangays" to GeoPoint(14.0667, 120.6333),

        "Aga" to GeoPoint(14.0800, 120.6800),
        "Balaytigui" to GeoPoint(14.1000, 120.7000),
        "Banilad" to GeoPoint(14.0800, 120.6200),

        "Barangay 1" to GeoPoint(14.0667, 120.6333),
        "Barangay 2" to GeoPoint(14.0667, 120.6333),
        "Barangay 3" to GeoPoint(14.0667, 120.6333),
        "Barangay 4" to GeoPoint(14.0667, 120.6333),
        "Barangay 5" to GeoPoint(14.0667, 120.6333),
        "Barangay 6" to GeoPoint(14.0667, 120.6333),
        "Barangay 7" to GeoPoint(14.0667, 120.6333),
        "Barangay 8" to GeoPoint(14.0667, 120.6333),
        "Barangay 9" to GeoPoint(14.0667, 120.6333),
        "Barangay 10" to GeoPoint(14.0667, 120.6333),
        "Barangay 11" to GeoPoint(14.0667, 120.6333),
        "Barangay 12" to GeoPoint(14.0667, 120.6333),

        "Bilaran" to GeoPoint(14.0900, 120.6500),
        "Bucana" to GeoPoint(14.0400, 120.6800),
        "Bulihan" to GeoPoint(14.0600, 120.6300),
        "Bunducan" to GeoPoint(14.1000, 120.6500),
        "Butucan" to GeoPoint(14.0500, 120.6100),
        "Calayo" to GeoPoint(14.1600, 120.5900),
        "Catandaan" to GeoPoint(14.0900, 120.6200),
        "Cogunan" to GeoPoint(14.1200, 120.6400),
        "Dayap" to GeoPoint(14.0500, 120.6400),
        "Kaylaway" to GeoPoint(14.1000, 120.6800),
        "Kayrilaw" to GeoPoint(14.1400, 120.6200),
        "Latag" to GeoPoint(14.0600, 120.6000),
        "Looc" to GeoPoint(14.0800, 120.6000),
        "Lumbangan" to GeoPoint(14.0500, 120.6500),
        "Malapad na Bato" to GeoPoint(14.1200, 120.6100),
        "Maugat" to GeoPoint(14.0900, 120.6700),
        "Munting Indan" to GeoPoint(14.1100, 120.6200),
        "Natipuan" to GeoPoint(14.1800, 120.5900),
        "Pantalan" to GeoPoint(14.0700, 120.6400),
        "Papaya" to GeoPoint(14.1700, 120.6100),
        "Putat" to GeoPoint(14.0700, 120.6100),
        "Reparo" to GeoPoint(14.1100, 120.6500),
        "Talangan" to GeoPoint(14.1300, 120.6300),
        "Tumalim" to GeoPoint(14.1200, 120.6800),
        "Utod" to GeoPoint(14.1000, 120.6000),
        "Wawa" to GeoPoint(14.0667, 120.6333)
    )

    private var selectedBarangay = "All Barangays"

    private var allFarms: List<Farm> = emptyList()

    // Only barangays containing registered farms
    private var availableBarangays: List<String> = emptyList()

    // =====================================
    // ON CREATE
    // =====================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupOSMConfiguration()

        setContentView(R.layout.activity_farm_map)

        mapView = findViewById(R.id.mapView)
        spBarangay = findViewById(R.id.spBarangay)

        setupMap()

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadFarmMarkers()

        loadFarmMarkers()
    }

    // =====================================
    // OSM CONFIGURATION
    // =====================================

    private fun setupOSMConfiguration() {

        val config = Configuration.getInstance()

        config.userAgentValue =
            "AgriFarm/1.0 (com.example.agricultural)"

        val osmFolder = File(filesDir, "osmdroid")
        val tileFolder = File(osmFolder, "tiles")

        osmFolder.mkdirs()
        tileFolder.mkdirs()

        config.osmdroidBasePath = osmFolder
        config.osmdroidTileCache = tileFolder
    }

    // =====================================
    // SETUP MAP
    // =====================================

    private fun setupMap() {

        mapView.setTileSource(TileSourceFactory.MAPNIK)

        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(true)

        mapView.setMinZoomLevel(11.0)
        mapView.setMaxZoomLevel(19.0)

        mapView.controller.setZoom(13.0)

        mapView.controller.setCenter(
            GeoPoint(14.0667, 120.6333)
        )

        mapView.overlays.add(CopyrightOverlay(this))

        mapView.invalidate()
    }

    // =====================================
    // LOAD FARM DATABASE
    // =====================================

    private fun loadFarmMarkers() {

        lifecycleScope.launch {

            try {

                allFarms = dao.getAll()

                // Only Nasugbu farms
                val nasugbuFarms = allFarms.filter {
                    it.farmAddress.contains(
                        "Nasugbu",
                        ignoreCase = true
                    )
                }

                // Get barangays that actually have farms
                availableBarangays = nasugbuFarms
                    .mapNotNull { farm ->

                        barangayLocations.keys
                            .filter { it != "All Barangays" }
                            .firstOrNull { barangay ->
                                belongsToBarangay(
                                    farm.farmAddress,
                                    barangay
                                )
                            }
                    }
                    .distinct()
                    .sorted()

                // Setup spinner dynamically
                setupBarangaySpinner()

                // Show all farms initially
                selectedBarangay = "All Barangays"

                displayFilteredFarms()

            } catch (e: Exception) {

                Toast.makeText(
                    this@FarmMapActivity,
                    "Unable to load farms: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // =====================================
    // BARANGAY SPINNER
    // =====================================


    private fun setupBarangaySpinner() {

        val barangays = listOf("All Barangays") + availableBarangays

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            barangays
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spBarangay.adapter = adapter

        spBarangay.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedBarangay = barangays[position]
                    moveToBarangay(selectedBarangay)
                    displayFilteredFarms()
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {}
            }
    }

    private var selectedFarmId: Int = 0

    private val editFarmPhotoPicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    lifecycleScope.launch {
                        try {
                            val result = dao.updateFarmPhoto(
                                selectedFarmId,
                                uri.toString()
                            )

                            if (result > 0) {
                                Toast.makeText(
                                    this@FarmMapActivity,
                                    "Farm photo updated!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                loadFarmMarkers()
                            } else {
                                Toast.makeText(
                                    this@FarmMapActivity,
                                    "Farm not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@FarmMapActivity,
                                "Unable to update photo: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Unable to access photo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    // =====================================
    // NORMALIZE ADDRESS
    // =====================================

    private fun normalizeAddress(address: String): String {

        return address
            .lowercase(Locale.ROOT)
            .replace(
                Regex("\\bbarangay\\b"),
                " "
            )
            .replace(
                Regex("\\bbrgy\\.?\\b"),
                " "
            )
            .replace(",", " ")
            .replace(".", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // =====================================
    // CHECK BARANGAY
    // =====================================

    private fun belongsToBarangay(
        address: String,
        barangay: String
    ): Boolean {

        val normalizedAddress = normalizeAddress(address)
        val normalizedBarangay = normalizeAddress(barangay)

        val addressParts = normalizedAddress
            .split(Regex("[,\\s]+"))
            .filter { it.isNotBlank() }

        val barangayParts = normalizedBarangay
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        return if (barangayParts.size == 1) {

            addressParts.contains(normalizedBarangay)

        } else {

            normalizedAddress.contains(normalizedBarangay)
        }
    }

    // =====================================
    // MOVE MAP TO BARANGAY
    // =====================================

    private fun moveToBarangay(barangay: String) {

        if (barangay == "All Barangays") {

            mapView.controller.setZoom(12.0)

            mapView.controller.animateTo(
                GeoPoint(14.0667, 120.6333)
            )

            return
        }

        // Find registered farms in selected barangay
        val farmsInBarangay = allFarms.filter { farm ->

            farm.farmAddress.contains(
                "Nasugbu",
                ignoreCase = true
            ) && belongsToBarangay(
                farm.farmAddress,
                barangay
            )
        }

        if (farmsInBarangay.isEmpty()) return

        // Center map using actual farm coordinates
        val centerLatitude = farmsInBarangay
            .map { it.latitude }
            .average()

        val centerLongitude = farmsInBarangay
            .map { it.longitude }
            .average()

        val location = GeoPoint(
            centerLatitude,
            centerLongitude
        )

        mapView.controller.setZoom(15.5)

        mapView.controller.animateTo(location)

        mapView.invalidate()
    }

    // =====================================
    // FILTER AND DISPLAY FARMS
    // =====================================

    private fun displayFilteredFarms() {

        // Remove previous farm markers
        mapView.overlays.removeAll {
            it is Marker
        }

        // Filter farms
        val filteredFarms = allFarms.filter { farm ->

            val isNasugbu = farm.farmAddress.contains(
                "Nasugbu",
                ignoreCase = true
            )

            val matchesBarangay =
                selectedBarangay == "All Barangays" ||
                        belongsToBarangay(
                            farm.farmAddress,
                            selectedBarangay
                        )

            isNasugbu && matchesBarangay
        }

        // Add farm markers
        filteredFarms.forEach { farm ->

            val marker = Marker(mapView)

            marker.position = GeoPoint(
                farm.latitude,
                farm.longitude
            )

            marker.title = farm.farmName

            marker.snippet =
                "Owner: ${farm.ownerName}\n" +
                        "Address: ${farm.farmAddress}"

            marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
            )

            marker.setOnMarkerClickListener { _, _ ->

                showFarmDetails(farm)

                true
            }

            mapView.overlays.add(marker)
        }

        mapView.invalidate()

        Toast.makeText(
            this,
            "${filteredFarms.size} farm(s) found in $selectedBarangay",
            Toast.LENGTH_SHORT
        ).show()
    }

    // =====================================
    // FARM DETAILS BOTTOM SHEET
    // =====================================




    private fun showFarmDetails(farm: Farm) {

        val dialog = BottomSheetDialog(this)

        dialog.setContentView(
            R.layout.bottom_sheet_farm
        )

        // =================================
        // DISPLAY FARM PHOTO
        // =================================

        val imgFarm = dialog.findViewById<android.widget.ImageView>(
            R.id.imgFarm
        )

        if (farm.farmPhoto.isNotBlank()) {

            try {

                imgFarm?.setImageURI(
                    Uri.parse(farm.farmPhoto)
                )

                if (imgFarm?.drawable == null) {
                    imgFarm?.setImageResource(
                        R.drawable.farm_placeholder
                    )
                }

            } catch (e: Exception) {

                imgFarm?.setImageResource(
                    R.drawable.farm_placeholder
                )
            }

        } else {

            imgFarm?.setImageResource(
                R.drawable.farm_placeholder
            )
        }

        // =================================
        // FARM INFORMATION
        // =================================

        dialog.findViewById<TextView>(R.id.txtFarmName)
            ?.text = farm.farmName

        dialog.findViewById<TextView>(R.id.txtFarmCode)
            ?.text = "Farm Code: ${farm.farmCode}"

        dialog.findViewById<TextView>(R.id.txtOwner)
            ?.text = "Owner: ${farm.ownerName}"

        dialog.findViewById<TextView>(R.id.txtAddress)
            ?.text = "Address: ${farm.farmAddress}"

        dialog.findViewById<TextView>(R.id.txtContact)
            ?.text = if (farm.contactNumber.isBlank()) {
            "Contact: Not available"
        } else {
            "Contact: ${farm.contactNumber}"
        }

        dialog.findViewById<TextView>(R.id.txtCoordinates)
            ?.text = "Coordinates: ${farm.latitude}, ${farm.longitude}"

        // =================================
        // VIEW FARM DETAILS
        // =================================

        dialog.findViewById<Button>(R.id.btnViewDetails)
            ?.setOnClickListener {

                val intent = Intent(
                    this,
                    FarmDetailsActivity::class.java
                )

                intent.putExtra("farm_id", farm.id)

                dialog.dismiss()

                startActivity(intent)
            }
        dialog.findViewById<Button>(R.id.btnEditPhoto)
            ?.setOnClickListener {

                selectedFarmId = farm.id

                editFarmPhotoPicker.launch(
                    arrayOf("image/*")
                )
            }

        // =================================
        // GET DIRECTIONS
        // =================================

        dialog.findViewById<Button>(R.id.btnDirections)
            ?.setOnClickListener {

                val uri = Uri.parse(
                    "https://www.openstreetmap.org/directions?to=" +
                            "${farm.latitude}%2C${farm.longitude}"
                )

                try {

                    startActivity(
                        Intent(Intent.ACTION_VIEW, uri)
                    )

                } catch (e: Exception) {

                    Toast.makeText(
                        this,
                        "Unable to open directions",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        dialog.show()
    }
    //==========================
    // LIFECYCLE
    // =====================================

    override fun onResume() {
        super.onResume()

        if (::mapView.isInitialized) {
            mapView.onResume()
        }
    }

    override fun onPause() {

        if (::mapView.isInitialized) {
            mapView.onPause()
        }

        super.onPause()
    }

    override fun onDestroy() {

        if (::mapView.isInitialized) {
            mapView.onDetach()
        }

        super.onDestroy()
    }
}
