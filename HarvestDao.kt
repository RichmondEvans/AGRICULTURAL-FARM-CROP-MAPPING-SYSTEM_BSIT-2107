package com.example.agricultural

import android.content.ContentValues
import android.database.Cursor

class HarvestDao(helper: AppDatabase) :
    FarmScopedDao<Harvest>(helper, "harvests", "harvestId"),
    HarvestRepository {

    // The harvests table has no farmId, so it is taken from the crop.
    override val selectSql: String
        get() = """
            SELECT h.harvestId, h.cropId, h.harvestDate,
                   h.harvestQuality, h.availableQuantity,
                   c.farmId AS farmId
            FROM harvests h
            INNER JOIN crops c ON h.cropId = c.cropId
        """.trimIndent()

    override val farmColumn: String
        get() = "c.farmId"

    override fun toValues(item: Harvest) = ContentValues().apply {
        put("cropId", item.cropId)
        put("harvestDate", item.harvestDate)
        put("harvestQuality", item.harvestQuality)
        put("availableQuantity", item.availableQuantity)
    }

    override fun fromCursor(cursor: Cursor) = Harvest(
        id = cursor.int("harvestId"),
        farmId = cursor.int("farmId"),
        cropId = cursor.int("cropId"),
        harvestDate = cursor.string("harvestDate"),
        harvestQuality = cursor.string("harvestQuality"),
        availableQuantity = cursor.double("availableQuantity")
    )
}
