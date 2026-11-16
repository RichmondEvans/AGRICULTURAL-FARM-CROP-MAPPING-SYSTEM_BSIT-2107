
package com.example.agricultural

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "agrifarm.db", null, 3) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createAllTables(db)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        createAllTables(db)
    }

    private fun createAllTables(db: SQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS farms (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                farmName TEXT NOT NULL,
                ownerName TEXT NOT NULL,
                farmAddress TEXT NOT NULL,
                contactNumber TEXT NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS crops (
                cropId INTEGER PRIMARY KEY AUTOINCREMENT,
                farmId INTEGER NOT NULL,
                cropName TEXT NOT NULL,
                cropVariety TEXT NOT NULL,
                cropStatus TEXT NOT NULL,
                FOREIGN KEY(farmId) REFERENCES farms(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS production_areas (
                areaId INTEGER PRIMARY KEY AUTOINCREMENT,
                farmId INTEGER NOT NULL,
                areaUnit TEXT NOT NULL,
                soilType TEXT NOT NULL,
                currentLandStatus TEXT NOT NULL,
                FOREIGN KEY(farmId) REFERENCES farms(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS harvests (
                harvestId INTEGER PRIMARY KEY AUTOINCREMENT,
                cropId INTEGER NOT NULL,
                harvestDate TEXT NOT NULL,
                harvestQuality TEXT NOT NULL,
                availableQuantity REAL NOT NULL,
                FOREIGN KEY(cropId) REFERENCES crops(cropId) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS farm_business (
                businessId INTEGER PRIMARY KEY AUTOINCREMENT,
                farmId INTEGER NOT NULL,
                cropName TEXT NOT NULL,
                quantitySold REAL NOT NULL,
                price REAL NOT NULL,
                expenses REAL NOT NULL,
                FOREIGN KEY(farmId) REFERENCES farms(id) ON DELETE CASCADE
            )
        """.trimIndent())
    }

    fun farmDao(): FarmDao = FarmDao(this)

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppDatabase(context).also {
                    INSTANCE = it
                }
            }
    }
}
