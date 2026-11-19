package com.example.agricultural

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Map boundary (polygon points) of each crop. */
class BoundaryDao(private val helper: AppDatabase) : BoundaryRepository {

    override suspend fun save(
        cropId: Int,
        points: List<Pair<Double, Double>>
    ) {
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put("cropId", cropId)
                put("points", points.joinToString(";") { "${it.first},${it.second}" })
            }

            helper.writableDatabase.insertWithOnConflict(
                "crop_boundaries",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
        }
    }

    override suspend fun get(cropId: Int): List<Pair<Double, Double>> =
        withContext(Dispatchers.IO) {
            helper.readableDatabase.rawQuery(
                "SELECT points FROM crop_boundaries WHERE cropId = ?",
                arrayOf(cropId.toString())
            ).use { c ->
                if (c.moveToFirst()) parsePoints(c.getString(0)) else emptyList()
            }
        }

    override suspend fun delete(cropId: Int): Int =
        withContext(Dispatchers.IO) {
            helper.writableDatabase.delete(
                "crop_boundaries",
                "cropId = ?",
                arrayOf(cropId.toString())
            )
        }

    override suspend fun getForFarm(farmId: Int): List<CropBoundary> =
        withContext(Dispatchers.IO) {
            val list = mutableListOf<CropBoundary>()

            helper.readableDatabase.rawQuery(
                """
                SELECT c.cropId, c.cropName, b.points
                FROM crop_boundaries b
                INNER JOIN crops c ON b.cropId = c.cropId
                WHERE c.farmId = ?
                """.trimIndent(),
                arrayOf(farmId.toString())
            ).use { c ->
                while (c.moveToNext()) {
                    list.add(
                        CropBoundary(
                            cropId = c.getInt(0),
                            cropName = c.getString(1),
                            points = parsePoints(c.getString(2))
                        )
                    )
                }
            }

            list
        }

    private fun parsePoints(text: String): List<Pair<Double, Double>> =
        text.split(";").mapNotNull {
            val part = it.split(",")
            val lat = part.getOrNull(0)?.toDoubleOrNull()
            val lon = part.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && lon != null) Pair(lat, lon) else null
        }
}
