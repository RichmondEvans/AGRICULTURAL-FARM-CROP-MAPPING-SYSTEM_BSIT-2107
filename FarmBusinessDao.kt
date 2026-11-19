package com.example.agricultural

import android.content.ContentValues
import android.database.Cursor

class FarmBusinessDao(helper: AppDatabase) :
    FarmScopedDao<FarmBusiness>(helper, "farm_business", "businessId"),
    BusinessRepository {

    override fun toValues(item: FarmBusiness) = ContentValues().apply {
        put("farmId", item.farmId)
        put("cropName", item.cropName)
        put("quantitySold", item.quantitySold)
        put("price", item.price)
        put("expenses", item.expenses)
    }

    override fun fromCursor(cursor: Cursor) = FarmBusiness(
        id = cursor.int("businessId"),
        farmId = cursor.int("farmId"),
        cropName = cursor.string("cropName"),
        quantitySold = cursor.double("quantitySold"),
        price = cursor.double("price"),
        expenses = cursor.double("expenses")
    )
}
