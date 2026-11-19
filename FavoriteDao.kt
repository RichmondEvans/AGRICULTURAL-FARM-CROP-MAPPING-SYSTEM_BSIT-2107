package com.example.agricultural

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoriteDao(private val helper: AppDatabase) : FavoriteRepository {

    override suspend fun save(farmId: Int) {
        withContext(Dispatchers.IO) {
            helper.writableDatabase.insertWithOnConflict(
                "farm_favorites",
                null,
                ContentValues().apply { put("farmId", farmId) },
                SQLiteDatabase.CONFLICT_IGNORE
            )
        }
    }

    override suspend fun remove(farmId: Int): Int =
        withContext(Dispatchers.IO) {
            helper.writableDatabase.delete(
                "farm_favorites",
                "farmId = ?",
                arrayOf(farmId.toString())
            )
        }

    override suspend fun isFavorite(farmId: Int): Boolean =
        withContext(Dispatchers.IO) {
            helper.readableDatabase.rawQuery(
                "SELECT 1 FROM farm_favorites WHERE farmId = ?",
                arrayOf(farmId.toString())
            ).use { it.moveToFirst() }
        }

    override suspend fun getFavoriteFarms(): List<Farm> =
        withContext(Dispatchers.IO) {
            val farms = mutableListOf<Farm>()

            helper.readableDatabase.rawQuery(
                """
                SELECT f.id, f.farmName, f.ownerName, f.farmAddress,
                       f.contactNumber, f.latitude, f.longitude,
                       f.farmPhoto, f.website
                FROM farms f
                INNER JOIN farm_favorites ff ON f.id = ff.farmId
                WHERE f.isDeleted = 0
                ORDER BY f.farmName
                """.trimIndent(),
                null
            ).use { c ->
                while (c.moveToNext()) farms.add(c.toFarm())
            }

            farms
        }
}
