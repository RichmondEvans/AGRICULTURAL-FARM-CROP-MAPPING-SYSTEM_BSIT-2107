package com.example.agricultural

import android.content.ContentValues
import android.database.Cursor

class FarmDao(helper: AppDatabase) :
    BaseDao<Farm>(helper, "farms", "id"),
    FarmRepository {

    override val selectSql: String
        get() = "SELECT $FARM_COLUMNS FROM farms"

    override fun toValues(item: Farm) = ContentValues().apply {
        put("farmName", item.farmName)
        put("ownerName", item.ownerName)
        put("farmAddress", item.farmAddress)
        put("contactNumber", item.contactNumber)
        put("latitude", item.latitude)
        put("longitude", item.longitude)
        put("farmPhoto", item.farmPhoto)
        put("website", item.website)
    }

    override fun fromCursor(cursor: Cursor) = cursor.toFarm()

    override suspend fun getAll(): List<Farm> = io {
        query("$selectSql WHERE isDeleted = 0 ORDER BY id ASC")
    }

    override suspend fun search(keyword: String): List<Farm> {
        val text = keyword.trim()
        if (text.isEmpty()) return getAll()

        val like = "%$text%"
        return io {
            query(
                """
                $selectSql
                WHERE isDeleted = 0
                  AND (farmName LIKE ? COLLATE NOCASE
                   OR ownerName LIKE ? COLLATE NOCASE
                   OR farmAddress LIKE ? COLLATE NOCASE)
                ORDER BY farmName COLLATE NOCASE ASC
                """.trimIndent(),
                arrayOf(like, like, like)
            )
        }
    }

    override suspend fun count(): Int = io {
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM farms WHERE isDeleted = 0",
            null
        ).use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    override suspend fun updateFarmPhoto(
        farmId: Int,
        photoUri: String
    ): Int = io {
        helper.writableDatabase.update(
            "farms",
            ContentValues().apply { put("farmPhoto", photoUri) },
            "id = ?",
            arrayOf(farmId.toString())
        )
    }

    // ---- Recycle Bin ----

    override suspend fun getTrashFarms(): List<Farm> = io {
        query("$selectSql WHERE isDeleted = 1 ORDER BY farmName COLLATE NOCASE ASC")
    }

    override suspend fun moveToTrash(farmId: Int) = setDeleted(farmId, 1)

    override suspend fun restoreFarm(farmId: Int) = setDeleted(farmId, 0)

    private suspend fun setDeleted(farmId: Int, value: Int): Int = io {
        helper.writableDatabase.update(
            "farms",
            ContentValues().apply { put("isDeleted", value) },
            "id = ?",
            arrayOf(farmId.toString())
        )
    }

    companion object {
        const val FARM_COLUMNS =
            "id, farmName, ownerName, farmAddress, contactNumber, " +
                "latitude, longitude, farmPhoto, website"
    }
}

/** Shared by FarmDao and FavoriteDao. */
internal fun Cursor.toFarm() = Farm(
    id = int("id"),
    farmName = string("farmName"),
    ownerName = string("ownerName"),
    farmAddress = string("farmAddress"),
    contactNumber = string("contactNumber"),
    latitude = double("latitude"),
    longitude = double("longitude"),
    farmPhoto = string("farmPhoto"),
    website = string("website")
)
