
package com.example.agricultural

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.agricultural.databinding.ActivityFarmDetailsBinding
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.appcompat.app.AlertDialog
import android.graphics.Color
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polygon

class FarmDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFarmDetailsBinding
    private lateinit var farmMap: MapView

    private var farmId: Int = -1

    private val boundaryOverlays = mutableListOf<Overlay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName

        binding = ActivityFarmDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        farmId = intent.getIntExtra("farm_id", -1)

        // Initialize OpenStreetMap
        farmMap = binding.farmMap

        farmMap.setMultiTouchControls(true)
        farmMap.setUseDataConnection(true)

        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnDeleteFarm.setOnClickListener {
            confirmDeleteFarm()
        }

        // Farm management modules
        binding.cardCrops.setOnClickListener {
            openModule(CropsActivity::class.java)
        }

        binding.cardAreas.setOnClickListener {
            openModule(ProductionAreaActivity::class.java)
        }

        binding.cardHarvest.setOnClickListener {
            openModule(HarvestActivity::class.java)
        }

        binding.cardBusiness.setOnClickListener {
            openModule(FarmBusinessActivity::class.java)
        }

        binding.btnDrawCropAreas.setOnClickListener {
            openModule(CropAreaActivity::class.java)
        }

        loadFarmDetails()
    }

    // Open farm management modules
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

    // Load farm information from Room Database
    private fun loadFarmDetails() {

        lifecycleScope.launch {

            try {

                val farm = AppDatabase.get(this@FarmDetailsActivity)
                    .farmDao()
                    .getById(farmId)

                if (farm == null) {

                    Toast.makeText(
                        this@FarmDetailsActivity,
                        "Farm not found",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                    return@launch
                }

                // Display farm information
                binding.tvFarmName.text = farm.farmName
                binding.tvFarmId.text = "Farm ID: ${farm.farmCode}"
                binding.tvOwner.text = farm.ownerName
                binding.tvContact.text = farm.contactNumber
                binding.tvAddress.text = farm.farmAddress

                // Retrieve farm coordinates
                // Assumes latitude and longitude are Double properties.
                val latitude = farm.latitude
                val longitude = farm.longitude

                if (
                    latitude.isFinite() &&
                    longitude.isFinite() &&
                    latitude in -90.0..90.0 &&
                    longitude in -180.0..180.0 &&
                    (latitude != 0.0 || longitude != 0.0)
                ) {

                    binding.tvLatitude.text =
                        "Latitude: $latitude"

                    binding.tvLongitude.text =
                        "Longitude: $longitude"

                    // Display satellite map
                    displayFarmMap(
                        latitude,
                        longitude,
                        farm.farmName
                    )

                } else {

                    binding.tvLatitude.text =
                        "Latitude: Not available"

                    binding.tvLongitude.text =
                        "Longitude: Not available"

                    Toast.makeText(
                        this@FarmDetailsActivity,
                        "Invalid or missing farm coordinates",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@FarmDetailsActivity,
                    "Unable to load farm details: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Display the crop area with crop name
    private fun loadCropBoundaries() {

        if (farmId <= 0) return

        lifecycleScope.launch {
            try {
                val boundaries = AppDatabase.get(this@FarmDetailsActivity)
                    .boundaryDao()
                    .getForFarm(farmId)


                farmMap.overlays.removeAll(boundaryOverlays)
                boundaryOverlays.clear()

                val colors = listOf(
                    Color.rgb(255, 87, 34),
                    Color.rgb(33, 150, 243),
                    Color.rgb(255, 235, 59),
                    Color.rgb(156, 39, 176),
                    Color.rgb(0, 200, 83)
                )

                boundaries.forEachIndexed { index, boundary ->

                    if (boundary.points.size < 3) return@forEachIndexed

                    val color = colors[index % colors.size]

                    val geoPoints = boundary.points.map {
                        GeoPoint(it.first, it.second)
                    }

                    // Outline at kulay ng area
                    val polygon = Polygon(farmMap)
                    polygon.points = geoPoints
                    polygon.fillPaint.color = Color.argb(
                        90,
                        Color.red(color),
                        Color.green(color),
                        Color.blue(color)
                    )
                    polygon.outlinePaint.color = color
                    polygon.outlinePaint.strokeWidth = 5f
                    polygon.title = boundary.cropName

                    // Pangalan ng crop sa gitna ng area
                    val label = Marker(farmMap)
                    label.position = GeoPoint(
                        boundary.points.map { it.first }.average(),
                        boundary.points.map { it.second }.average()
                    )
                    label.setTextIcon(boundary.cropName)
                    label.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_CENTER
                    )

                    boundaryOverlays.add(polygon)
                    boundaryOverlays.add(label)
                }

                // Ilagay sa ilalim ng farm marker
                farmMap.overlays.addAll(0, boundaryOverlays)
                farmMap.invalidate()

            } catch (e: Exception) {
                Toast.makeText(
                    this@FarmDetailsActivity,
                    "Unable to load crop areas: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun confirmDeleteFarm() {
        AlertDialog.Builder(this)
            .setTitle("Delete Farm")
            .setMessage("Ilipat ang farm na ito sa Recycle Bin?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    AppDatabase.get(this@FarmDetailsActivity)
                        .farmDao()
                        .moveToTrash(farmId)

                    Toast.makeText(
                        this@FarmDetailsActivity,
                        "Farm moved to Recycle Bin",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Display satellite imagery and farm marker
    private fun displayFarmMap(
        latitude: Double,
        longitude: Double,
        farmName: String
    ) {

        val satelliteSource = object : XYTileSource(
            "EsriSatellite",
            0,
            19,
            256,
            ".jpg",
            arrayOf("https://services.arcgisonline.com/")
        ) {

            override fun getTileURLString(
                tileIndex: Long
            ): String {

                val zoom = MapTileIndex.getZoom(tileIndex)
                val x = MapTileIndex.getX(tileIndex)
                val y = MapTileIndex.getY(tileIndex)

                return "https://services.arcgisonline.com/" +
                        "ArcGIS/rest/services/World_Imagery/" +
                        "MapServer/tile/$zoom/$y/$x"
            }
        }

        // Set satellite map
        farmMap.setTileSource(satelliteSource)

        // Farm coordinates
        val farmLocation = GeoPoint(
            latitude,
            longitude
        )

        // Center map on the farm
        farmMap.controller.setZoom(17.0)
        farmMap.controller.setCenter(farmLocation)

        // Create marker
        val marker = Marker(farmMap)

        marker.position = farmLocation
        marker.title = farmName
        marker.snippet =
            "Latitude: $latitude\nLongitude: $longitude"

        marker.setAnchor(
            Marker.ANCHOR_CENTER,
            Marker.ANCHOR_BOTTOM
        )

        // Add marker to map
        farmMap.overlays.add(marker)

        marker.showInfoWindow()

        farmMap.invalidate()
    }

    // Map lifecycle
    override fun onResume() {
        super.onResume()

        if (::farmMap.isInitialized) {
            farmMap.onResume()
            loadCropBoundaries()
        }
    }

    override fun onPause() {

        if (::farmMap.isInitialized) {
            farmMap.onPause()
        }

        super.onPause()
    }

    override fun onDestroy() {

        if (::farmMap.isInitialized) {
            farmMap.onDetach()
        }

        super.onDestroy()
    }
}
