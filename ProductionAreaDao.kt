package com.example.agricultural

import android.content.ContentValues
import android.database.Cursor

class ProductionAreaDao(helper: AppDatabase) :
    FarmScopedDao<ProductionArea>(helper, "production_areas", "areaId"),
    ProductionAreaRepository {

    override fun toValues(item: ProductionArea) = ContentValues().apply {
        put("farmId", item.farmId)
        put("areaUnit", item.areaUnit)
        put("soilType", item.soilType)
        put("currentLandStatus", item.currentLandStatus)
    }

    override fun fromCursor(cursor: Cursor) = ProductionArea(
        id = cursor.int("areaId"),
        farmId = cursor.int("farmId"),
        areaUnit = cursor.string("areaUnit"),
        soilType = cursor.string("soilType"),
        currentLandStatus = cursor.string("currentLandStatus")
    )
}
