package com.example.agricultural

import android.content.ContentValues
import android.database.Cursor

class CropDao(helper: AppDatabase) :
    FarmScopedDao<Crop>(helper, "crops", "cropId"),
    CropRepository {

    override fun toValues(item: Crop) = ContentValues().apply {
        put("farmId", item.farmId)
        put("cropName", item.cropName)
        put("cropVariety", item.cropVariety)
        put("cropStatus", item.cropStatus)
    }

    override fun fromCursor(cursor: Cursor) = Crop(
        id = cursor.int("cropId"),
        farmId = cursor.int("farmId"),
        cropName = cursor.string("cropName"),
        cropVariety = cursor.string("cropVariety"),
        cropStatus = cursor.string("cropStatus")
    )
}
