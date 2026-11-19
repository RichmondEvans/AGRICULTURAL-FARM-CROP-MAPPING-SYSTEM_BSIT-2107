package com.example.agricultural

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

class CropAreaActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.get(this) }
    private val farmRepo by lazy { database.farmDao() }
    private val cropRepo by lazy { database.cropDao() }
    private val boundaryRepo by lazy { database.boundaryDao() }

    private var farmId = -1
    private var crops: List<Crop> = emptyList()
    private var selectedCrop: Crop? = null

    // MAP MARKER
    private val points = mutableListOf<GeoPoint>()

    private lateinit var mapView: MapView
    private lateinit var spCrop: Spinner
    private lateinit var tvHelp: TextView


    private val tapReceiver = object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            if (p != null && selectedCrop != null) {
                points.add(p)
                redraw()
            }
            return true
        }

        override fun longPressHelper(p: GeoPoint?): Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName

        farmId = intent.getIntExtra("farm_id", -1)

        if (farmId <= 0) {
            Toast.makeText(this, "Invalid Farm ID", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(this)
        title.text = "Draw Crop Area"
        title.textSize = 22f
        title.setPadding(24, 24, 24, 8)

        spCrop = Spinner(this)

        tvHelp = TextView(this)
        tvHelp.text = "Pumili ng crop, tapos i-tap ang mapa para ilagay ang mga gilid ng area."
        tvHelp.setPadding(24, 8, 24, 8)

        mapView = MapView(this)
        mapView.setMultiTouchControls(true)
        mapView.setUseDataConnection(true)
        mapView.setTileSource(satelliteSource())
        mapView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )

        // Unang row ng buttons
        val row1 = LinearLayout(this)
        row1.orientation = LinearLayout.HORIZONTAL
        row1.addView(makeButton("Undo") { undoPoint() })
        row1.addView(makeButton("Clear") { clearPoints() })
        row1.addView(makeButton("Delete Area") { deleteSavedArea() })

        // Pangalawang row ng buttons
        val row2 = LinearLayout(this)
        row2.orientation = LinearLayout.HORIZONTAL
        row2.addView(makeButton("Save Area") { saveArea() })
        row2.addView(makeButton("Back") { finish() })

        root.addView(title)
        root.addView(spCrop)
        root.addView(tvHelp)
        root.addView(mapView)
        root.addView(row1)
        root.addView(row2)

        setContentView(root)

        loadFarmAndCrops()
    }

    private fun satelliteSource() = object : XYTileSource(
        "EsriSatellite",
        0,
        19,
        256,
        ".jpg",
        arrayOf("https://services.arcgisonline.com/")
    ) {
        override fun getTileURLString(tileIndex: Long): String {
            val zoom = MapTileIndex.getZoom(tileIndex)
            val x = MapTileIndex.getX(tileIndex)
            val y = MapTileIndex.getY(tileIndex)

            return "https://services.arcgisonline.com/" +
                    "ArcGIS/rest/services/World_Imagery/" +
                    "MapServer/tile/$zoom/$y/$x"
        }
    }

    private fun makeButton(label: String, onClick: () -> Unit): Button {
        val button = Button(this)
        button.text = label
        button.isAllCaps = false
        button.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        button.setOnClickListener { onClick() }
        return button
    }

    // =====================================
    // CODE OF LOAD FARM AND CROPS
    // =====================================

    private fun loadFarmAndCrops() {
        lifecycleScope.launch {
            try {
                val farm = farmRepo.getById(farmId)

                if (farm == null) {
                    Toast.makeText(
                        this@CropAreaActivity,
                        "Farm not found",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                mapView.controller.setZoom(18.0)
                mapView.controller.setCenter(
                    GeoPoint(farm.latitude, farm.longitude)
                )

                crops = cropRepo.getByFarmId(farmId)

                if (crops.isEmpty()) {
                    Toast.makeText(
                        this@CropAreaActivity,
                        "Magdagdag muna ng crop sa Crops Information",
                        Toast.LENGTH_LONG
                    ).show()
                }

                val adapter = ArrayAdapter(
                    this@CropAreaActivity,
                    android.R.layout.simple_spinner_item,
                    crops.map { it.cropName }
                )
                adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )
                spCrop.adapter = adapter

                spCrop.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedCrop = crops[position]
                            loadSavedPoints()
                        }

                        override fun onNothingSelected(
                            parent: AdapterView<*>?
                        ) {}
                    }

                redraw()

            } catch (e: Exception) {
                Toast.makeText(
                    this@CropAreaActivity,
                    "Loading failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun loadSavedPoints() {
        val crop = selectedCrop ?: return

        lifecycleScope.launch {
            val saved = boundaryRepo.get(crop.id)

            points.clear()
            points.addAll(saved.map { GeoPoint(it.first, it.second) })

            redraw()
        }
    }

    // =====================================
    // DRAWING
    // =====================================

    private fun redraw() {
        mapView.overlays.clear()

        if (points.size >= 3) {
            val polygon = Polygon(mapView)
            polygon.points = points.toList()
            polygon.fillPaint.color = Color.argb(90, 255, 87, 34)
            polygon.outlinePaint.color = Color.rgb(255, 87, 34)
            polygon.outlinePaint.strokeWidth = 5f
            mapView.overlays.add(polygon)

        } else if (points.size == 2) {
            val line = Polyline(mapView)
            line.setPoints(points.toList())
            mapView.overlays.add(line)
        }

        points.forEachIndexed { index, point ->
            val marker = Marker(mapView)
            marker.position = point
            marker.setTextIcon("${index + 1}")
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            mapView.overlays.add(marker)
        }

        mapView.overlays.add(MapEventsOverlay(tapReceiver))

        tvHelp.text = "${points.size} points. You need at least 3."
        mapView.invalidate()
    }

    private fun undoPoint() {
        if (points.isNotEmpty()) {
            points.removeAt(points.lastIndex)
            redraw()
        }
    }

    private fun clearPoints() {
        points.clear()
        redraw()
    }

    // =====================================
    // CODE PARA SA SAVE / DELETE
    // =====================================

    private fun saveArea() {
        val crop = selectedCrop

        if (crop == null) {
            Toast.makeText(this, "Pumili muna ng crop", Toast.LENGTH_SHORT).show()
            return
        }

        if (points.size < 3) {
            Toast.makeText(
                this,
                "Kailangan ng hindi bababa sa 3 puntos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {
            try {
                boundaryRepo.save(
                    crop.id,
                    points.map { Pair(it.latitude, it.longitude) }
                )

                Toast.makeText(
                    this@CropAreaActivity,
                    "${crop.cropName} area saved!",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@CropAreaActivity,
                    "Save failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun deleteSavedArea() {
        val crop = selectedCrop ?: return

        lifecycleScope.launch {
            try {
                boundaryRepo.delete(crop.id)
                points.clear()
                redraw()

                Toast.makeText(
                    this@CropAreaActivity,
                    "Area deleted",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@CropAreaActivity,
                    "Delete failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // =====================================
    // MAP LIFECYCLE
    // =====================================

    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized) mapView.onResume()
    }

    override fun onPause() {
        if (::mapView.isInitialized) mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        if (::mapView.isInitialized) mapView.onDetach()
        super.onDestroy()
    }
}