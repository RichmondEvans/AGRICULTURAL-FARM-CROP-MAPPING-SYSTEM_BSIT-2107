package com.example.agricultural

import android.content.ContentValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FarmDao(private val db: AppDatabase) {

    private val columns =
        "id, farmName, ownerName, farmAddress, contactNumber, latitude, longitude"

    // ================= FARMS =================

    suspend fun insert(farm: Farm) {
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put("farmName", farm.farmName)
                put("ownerName", farm.ownerName)
                put("farmAddress", farm.farmAddress)
                put("contactNumber", farm.contactNumber)
                put("latitude", farm.latitude)
                put("longitude", farm.longitude)
            }
            db.writableDatabase.insert("farms", null, values)
        }
    }

    suspend fun getAll(): List<Farm> =
        withContext(Dispatchers.IO) {
            read("SELECT $columns FROM farms ORDER BY id ASC", null)
        }

    suspend fun search(q: String): List<Farm> =
        withContext(Dispatchers.IO) {
            val like = "%$q%"
            read(
                "SELECT $columns FROM farms WHERE farmName LIKE ? OR ownerName LIKE ? ORDER BY id ASC",
                arrayOf(like, like)
            )
        }

    suspend fun getById(id: Int): Farm? =
        withContext(Dispatchers.IO) {
            read(
                "SELECT $columns FROM farms WHERE id = ?",
                arrayOf(id.toString())
            ).firstOrNull()
        }

    suspend fun count(): Int =
        withContext(Dispatchers.IO) {
            db.readableDatabase.rawQuery(
                "SELECT COUNT(*) FROM farms", null
            ).use { c ->
                if (c.moveToFirst()) c.getInt(0) else 0
            }
        }

    // ================= CROPS =================

    suspend fun insertCrop(crop: Crop) {
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put("farmId", crop.farmId)
                put("cropName", crop.cropName)
                put("cropVariety", crop.cropVariety)
                put("cropStatus", crop.cropStatus)
            }
            db.writableDatabase.insert("crops", null, values)
        }
    }

    suspend fun getCrops(farmId: Int): List<Crop> =
        withContext(Dispatchers.IO) {
            val list = mutableListOf<Crop>()

            db.readableDatabase.rawQuery(
                "SELECT * FROM crops WHERE farmId = ?",
                arrayOf(farmId.toString())
            ).use { c ->
                while (c.moveToNext()) {
                    list.add(
                        Crop(
                            cropId = c.getInt(0),
                            farmId = c.getInt(1),
                            cropName = c.getString(2),
                            cropVariety = c.getString(3),
                            cropStatus = c.getString(4)
                        )
                    )
                }
            }
            list
        }

    suspend fun deleteCrop(cropId: Int) {
        withContext(Dispatchers.IO) {
            db.writableDatabase.delete(
                "crops", "cropId = ?",
                arrayOf(cropId.toString())
            )
        }
    }

    // ================= PRODUCTION AREAS =================

    suspend fun insertArea(area: ProductionArea) {
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put("farmId", area.farmId)
                put("areaUnit", area.areaUnit)
                put("soilType", area.soilType)
                put("currentLandStatus", area.currentLandStatus)
            }
            db.writableDatabase.insert(
                "production_areas", null, values
            )
        }
    }

    suspend fun getAreas(farmId: Int): List<ProductionArea> =
        withContext(Dispatchers.IO) {
            val list = mutableListOf<ProductionArea>()

            db.readableDatabase.rawQuery(
                "SELECT * FROM production_areas WHERE farmId = ?",
                arrayOf(farmId.toString())
            ).use { c ->
                while (c.moveToNext()) {
                    list.add(
                        ProductionArea(
                            areaId = c.getInt(0),
                            farmId = c.getInt(1),
                            areaUnit = c.getString(2),
                            soilType = c.getString(3),
                            currentLandStatus = c.getString(4)
                        )
                    )
                }
            }
            list
        }

    suspend fun deleteArea(areaId: Int) {
        withContext(Dispatchers.IO) {
            db.writableDatabase.delete(
                "production_areas", "areaId = ?",
                arrayOf(areaId.toString())
            )
        }
    }

    // ================= HARVEST RECORDS =================

    suspend fun insertHarvest(harvest: Harvest) {
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put("cropId", harvest.cropId)
                put("harvestDate", harvest.harvestDate)
                put("harvestQuality", harvest.harvestQuality)
                put("availableQuantity", harvest.availableQuantity)
            }
            db.writableDatabase.insert("harvests", null, values)
        }
    }

    suspend fun getHarvests(farmId: Int): List<Harvest> =
        withContext(Dispatchers.IO) {
            val list = mutableListOf<Harvest>()

            db.readableDatabase.rawQuery(
                """
                SELECT h.* FROM harvests h
                INNER JOIN crops c ON h.cropId = c.cropId
                WHERE c.farmId = ?
                """.trimIndent(),
                arrayOf(farmId.toString())
            ).use { c ->
                while (c.moveToNext()) {
                    list.add(
                        Harvest(
                            harvestId = c.getInt(0),
                            cropId = c.getInt(1),
                            harvestDate = c.getString(2),
                            harvestQuality = c.getString(3),
                            availableQuantity = c.getDouble(4)
                        )
                    )
                }
            }
            list
        }

    suspend fun deleteHarvest(harvestId: Int) {
        withContext(Dispatchers.IO) {
            db.writableDatabase.delete(
                "harvests", "harvestId = ?",
                arrayOf(harvestId.toString())
            )
        }
    }

    // ================= FARM BUSINESS =================

    suspend fun insertBusiness(business: FarmBusiness) {
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put("farmId", business.farmId)
                put("cropName", business.cropName)
                put("quantitySold", business.quantitySold)
                put("price", business.price)
                put("expenses", business.expenses)
            }
            db.writableDatabase.insert(
                "farm_business", null, values
            )
        }
    }

    suspend fun getBusiness(farmId: Int): List<FarmBusiness> =
        withContext(Dispatchers.IO) {
            val list = mutableListOf<FarmBusiness>()

            db.readableDatabase.rawQuery(
                "SELECT * FROM farm_business WHERE farmId = ?",
                arrayOf(farmId.toString())
            ).use { c ->
                while (c.moveToNext()) {
                    list.add(
                        FarmBusiness(
                            businessId = c.getInt(0),
                            farmId = c.getInt(1),
                            cropName = c.getString(2),
                            quantitySold = c.getDouble(3),
                            price = c.getDouble(4),
                            expenses = c.getDouble(5)
                        )
                    )
                }
            }
            list
        }

    suspend fun deleteBusiness(businessId: Int) {
        withContext(Dispatchers.IO) {
            db.writableDatabase.delete(
                "farm_business", "businessId = ?",
                arrayOf(businessId.toString())
            )
        }
    }

    suspend fun getTotalProfit(): Double =
        withContext(Dispatchers.IO) {
            db.readableDatabase.rawQuery(
                """
                SELECT SUM((quantitySold * price) - expenses)
                FROM farm_business
                """.trimIndent(),
                null
            ).use { c ->
                if (c.moveToFirst() && !c.isNull(0)) {
                    c.getDouble(0)
                } else {
                    0.0
                }
            }
        }

    // ================= READ FARMS =================

    private fun read(
        sql: String,
        args: Array<String>?
    ): List<Farm> {
        val list = mutableListOf<Farm>()

        db.readableDatabase.rawQuery(sql, args).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Farm(
                        id = c.getInt(0),
                        farmName = c.getString(1),
                        ownerName = c.getString(2),
                        farmAddress = c.getString(3),
                        contactNumber = c.getString(4),
                        latitude = c.getDouble(5),
                        longitude = c.getDouble(6)
                    )
                )
            }
        }
        return list
    }
}
