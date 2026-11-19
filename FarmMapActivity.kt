package com.example.agricultural

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.util.Locale

class FarmMapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var spBarangay: Spinner

    private val dao by lazy { AppDatabase.get(this).farmDao() }

    // Center point of nasugbu
    private val nasugbuCenter = GeoPoint(14.0667, 120.6333)

    // List of barangay in nasugbu
    private val barangayNames = listOf(
        "Aga", "Balaytigui", "Banilad",
        "Barangay 1", "Barangay 2", "Barangay 3", "Barangay 4",
        "Barangay 5", "Barangay 6", "Barangay 7", "Barangay 8",
        "Barangay 9", "Barangay 10", "Barangay 11", "Barangay 12",
        "Bilaran", "Bucana", "Bulihan", "Bunducan", "Butucan",
        "Calayo", "Catandaan", "Cogunan", "Dayap", "Kaylaway",
        "Kayrilaw", "Latag", "Looc", "Lumbangan", "Malapad na Bato",
        "Maugat", "Munting Indan", "Natipuan", "Pantalan", "Papaya",
        "Putat", "Reparo", "Talangan", "Tumalim", "Utod", "Wawa"
    )

    private var selectedBarangay = "All Barangays"
    private var selectedFarmId: Int = 0
    private var allFarms: List<Farm> = emptyList()

    // Barangays registered
    private var availableBarangays: List<String> = emptyList()

    // Photo picker to Edit Photo
    private val editFarmPhotoPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

            if (uri == null) return@registerForActivityResult

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                lifecycleScope.launch {
                    try {
                        val result = dao.updateFarmPhoto(selectedFarmId, uri.toString())

                        if (result > 0) {
                            toast("Farm photo updated!")
                            loadFarmMarkers()
                        } else {
                            toast("Farm not found")
                        }
                    } catch (e: Exception) {
                        toast("Unable to update photo: ${e.message}")
                    }
                }

            } catch (e: Exception) {
                toast("Unable to access photo")
            }
        }

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

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        loadFarmMarkers()
    }

    // =====================================
    // OSM CONFIGURATION
    // =====================================

    private fun setupOSMConfiguration() {
        val config = Configuration.getInstance()
        config.userAgentValue = "AgriFarm/1.0 (com.example.agricultural)"

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
        mapView.controller.setCenter(nasugbuCenter)

        mapView.overlays.add(CopyrightOverlay(this))
        mapView.invalidate()
    }

    // =====================================
    // LOAD FARMS FROM DATABASE
    // =====================================

    private fun loadFarmMarkers() {
        lifecycleScope.launch {
            try {
                allFarms = dao.getAll()

                // Mga barangay na may farm
                availableBarangays = allFarms
                    .filter { isNasugbu(it) }
                    .mapNotNull { farm ->
                        barangayNames.firstOrNull { belongsToBarangay(farm.farmAddress, it) }
                    }
                    .distinct()
                    .sorted()

                setupBarangaySpinner()

                selectedBarangay = "All Barangays"
                displayFilteredFarms()

            } catch (e: Exception) {
                toast("Unable to load farms: ${e.message}")
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
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spBarangay.adapter = adapter

        spBarangay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

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

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // =====================================
    // ADDRESS HELPERS
    // =====================================

    private fun isNasugbu(farm: Farm): Boolean {
        return farm.farmAddress.contains("Nasugbu", ignoreCase = true)
    }

    private fun normalizeAddress(address: String): String {
        return address
            .lowercase(Locale.ROOT)
            .replace(Regex("\\bbarangay\\b"), " ")
            .replace(Regex("\\bbrgy\\.?\\b"), " ")
            .replace(",", " ")
            .replace(".", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun belongsToBarangay(address: String, barangay: String): Boolean {
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
            mapView.controller.animateTo(nasugbuCenter)
            return
        }

        val farmsInBarangay = allFarms.filter {
            isNasugbu(it) && belongsToBarangay(it.farmAddress, barangay)
        }

        if (farmsInBarangay.isEmpty()) return

        // Center Point
        val location = GeoPoint(
            farmsInBarangay.map { it.latitude }.average(),
            farmsInBarangay.map { it.longitude }.average()
        )

        mapView.controller.setZoom(15.5)
        mapView.controller.animateTo(location)
        mapView.invalidate()
    }

    // =====================================
    // DISPLAY FARM MARKERS
    // =====================================

    private fun displayFilteredFarms() {

        // Remove farm markers.
        mapView.overlays.removeAll { it is Marker }

        val filteredFarms = allFarms.filter { farm ->
            isNasugbu(farm) && (
                    selectedBarangay == "All Barangays" ||
                            belongsToBarangay(farm.farmAddress, selectedBarangay)
                    )
        }

        filteredFarms.forEach { farm ->

            val marker = Marker(mapView)

            marker.position = GeoPoint(farm.latitude, farm.longitude)
            marker.title = farm.farmName
            marker.snippet = "Owner: ${farm.ownerName}\nAddress: ${farm.farmAddress}"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            marker.setOnMarkerClickListener { _, _ ->
                showFarmDetails(farm)
                true
            }

            mapView.overlays.add(marker)
        }

        mapView.invalidate()

        toast("${filteredFarms.size} farm(s) found in $selectedBarangay")
    }

    // =====================================
    // FARM DETAILS BOTTOM SHEET
    // =====================================

    private fun showFarmDetails(farm: Farm) {

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.bottom_sheet_farm)

        // Farm photo
        val imgFarm = dialog.findViewById<ImageView>(R.id.imgFarm)

        if (farm.farmPhoto.isNotBlank()) {
            try {
                imgFarm?.setImageURI(Uri.parse(farm.farmPhoto))

                if (imgFarm?.drawable == null) {
                    imgFarm?.setImageResource(R.drawable.farm_placeholder)
                }
            } catch (e: Exception) {
                imgFarm?.setImageResource(R.drawable.farm_placeholder)
            }
        } else {
            imgFarm?.setImageResource(R.drawable.farm_placeholder)
        }

        // Farm information
        dialog.findViewById<TextView>(R.id.txtFarmName)?.text = farm.farmName
        dialog.findViewById<TextView>(R.id.txtFarmCode)?.text = "Farm Code: ${farm.farmCode}"
        dialog.findViewById<TextView>(R.id.txtOwner)?.text = "Owner: ${farm.ownerName}"
        dialog.findViewById<TextView>(R.id.txtAddress)?.text = "Address: ${farm.farmAddress}"
        dialog.findViewById<TextView>(R.id.txtContact)?.text =
            if (farm.contactNumber.isBlank()) "Contact: Not available"
            else "Contact: ${farm.contactNumber}"
        dialog.findViewById<TextView>(R.id.txtCoordinates)?.text =
            "Coordinates: ${farm.latitude}, ${farm.longitude}"

        // View Details button
        dialog.findViewById<Button>(R.id.btnViewDetails)?.setOnClickListener {
            val intent = Intent(this, FarmDetailsActivity::class.java)
            intent.putExtra("farm_id", farm.id)

            dialog.dismiss()
            startActivity(intent)
        }

        // Edit Photo button
        dialog.findViewById<Button>(R.id.btnEditPhoto)?.setOnClickListener {
            selectedFarmId = farm.id
            editFarmPhotoPicker.launch(arrayOf("image/*"))
        }

        // Directions button
        dialog.findViewById<Button>(R.id.btnDirections)?.setOnClickListener {
            val uri = Uri.parse(
                "https://www.openstreetmap.org/directions?to=${farm.latitude}%2C${farm.longitude}"
            )

            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: Exception) {
                toast("Unable to open directions")
            }
        }

        dialog.show()
    }

    // =====================================
    // SHORTCUT TO TOAST
    // =====================================

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // =====================================
    // LIFECYCLE
    // =====================================

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDetach()
        super.onDestroy()
    }
}