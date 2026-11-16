
package com.example.agricultural

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.CopyrightOverlay
import java.io.File

class FarmMapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    private val dao by lazy {
        AppDatabase.get(this).farmDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupOSMConfiguration()

        setContentView(R.layout.activity_farm_map)

        mapView = findViewById(R.id.mapView)

        setupMap()

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadFarmMarkers()
    }


    private fun setupOSMConfiguration() {

        val config = Configuration.getInstance()

        config.userAgentValue =
            "AgriFarm/1.0 (${applicationContext.packageName}; contact: richmondevansramirez@gmail.com)"

        val osmFolder = File(filesDir, "osmdroid")
        val tileFolder = File(osmFolder, "tiles")

        osmFolder.mkdirs()
        tileFolder.mkdirs()

        config.osmdroidBasePath = osmFolder
        config.osmdroidTileCache = tileFolder
    }


    private fun setupMap() {

        mapView.setTileSource(TileSourceFactory.MAPNIK)

        mapView.setMultiTouchControls(true)

        mapView.setBuiltInZoomControls(true)

        mapView.controller.setZoom(13.0)

        val nasugbu = GeoPoint(
            14.0667,
            120.6333
        )

        mapView.controller.setCenter(nasugbu)

        mapView.overlays.add(
            CopyrightOverlay(this)
        )

        mapView.invalidate()
    }

    private fun loadFarmMarkers() {

        lifecycleScope.launch {

            try {

                val farms = dao.getAll()

                mapView.overlays.removeAll {
                    it is Marker
                }

                // Only farms whose saved address identifies
                // Nasugbu are displayed.
                val nasugbuFarms = farms.filter {
                    it.farmAddress.contains(
                        "Nasugbu",
                        ignoreCase = true
                    )
                }

                for (farm in nasugbuFarms) {

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

                    mapView.overlays.add(marker)
                }

                mapView.invalidate()

                if (nasugbuFarms.isEmpty()) {
                    Toast.makeText(
                        this@FarmMapActivity,
                        "No registered farms in Nasugbu",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@FarmMapActivity,
                    "Unable to load farms: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

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
