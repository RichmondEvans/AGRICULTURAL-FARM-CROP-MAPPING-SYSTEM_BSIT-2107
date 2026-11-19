package com.example.agricultural

import android.content.ContentValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FarmPhotoDao(private val helper: AppDatabase) : FarmPhotoRepository {

    override suspend fun add(farmId: Int, photoUri: String): Long =
        withContext(Dispatchers.IO) {
            helper.writableDatabase.insertOrThrow(
                "farm_photos",
                null,
                ContentValues().apply {
                    put("farmId", farmId)
                    put("photoUri", photoUri)
                }
            )
        }

    override suspend fun getByFarm(farmId: Int): List<String> =
        withContext(Dispatchers.IO) {
            val photos = mutableListOf<String>()

            helper.readableDatabase.rawQuery(
                "SELECT photoUri FROM farm_photos WHERE farmId = ? ORDER BY photoId",
                arrayOf(farmId.toString())
            ).use { c ->
                while (c.moveToNext()) photos.add(c.getString(0))
            }

            photos
        }
}
