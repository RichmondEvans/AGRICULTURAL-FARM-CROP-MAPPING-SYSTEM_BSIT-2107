
package com.example.agricultural

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(
        context.applicationContext,
        "agrifarm.db",
        null,
        9
    ) {

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

        if (oldVersion < 4) {
            addColumnIfMissing(
                db,
                "farms",
                "farmPhoto",
                "TEXT NOT NULL DEFAULT ''"
            )
        }

        if (oldVersion < 5) {
            createUsersTable(db)
        }

        if (oldVersion < 6) {
            addColumnIfMissing(
                db,
                "farms",
                "website",
                "TEXT NOT NULL DEFAULT ''"
            )

            createFarmFavoritesTable(db)
            createFarmPhotosTable(db)
        }
        if (oldVersion < 8) {
            addColumnIfMissing(
                db,
                "farms",
                "isDeleted",
                "INTEGER NOT NULL DEFAULT 0"
            )
        }

        if (oldVersion < 9) {
            createCropBoundariesTable(db)
        }

        // Existing crops, harvests and production areas
        // are preserved.
    }

    private fun addColumnIfMissing(
        db: SQLiteDatabase,
        table: String,
        column: String,
        definition: String
    ) {

        val exists = db.rawQuery(
            "PRAGMA table_info($table)",
            null
        ).use { cursor ->

            var found = false

            while (cursor.moveToNext()) {
                if (cursor.getString(1) == column) {
                    found = true
                    break
                }
            }

            found
        }

        if (!exists) {
            db.execSQL(
                "ALTER TABLE $table ADD COLUMN $column $definition"
            )
        }
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
                longitude REAL NOT NULL,
                farmPhoto TEXT NOT NULL DEFAULT '',
                website TEXT NOT NULL DEFAULT '',
                isDeleted INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS crops (
                cropId INTEGER PRIMARY KEY AUTOINCREMENT,
                farmId INTEGER NOT NULL,
                cropName TEXT NOT NULL,
                cropVariety TEXT NOT NULL,
                cropStatus TEXT NOT NULL,
                FOREIGN KEY(farmId)
                REFERENCES farms(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS production_areas (
                areaId INTEGER PRIMARY KEY AUTOINCREMENT,
                farmId INTEGER NOT NULL,
                areaUnit TEXT NOT NULL,
                soilType TEXT NOT NULL,
                currentLandStatus TEXT NOT NULL,
                FOREIGN KEY(farmId)
                REFERENCES farms(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS harvests (
                harvestId INTEGER PRIMARY KEY AUTOINCREMENT,
                cropId INTEGER NOT NULL,
                harvestDate TEXT NOT NULL,
                harvestQuality TEXT NOT NULL,
                availableQuantity REAL NOT NULL,
                FOREIGN KEY(cropId)
                REFERENCES crops(cropId) ON DELETE CASCADE
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
                FOREIGN KEY(farmId)
                REFERENCES farms(id) ON DELETE CASCADE
            )
        """.trimIndent())

        createUsersTable(db)
        createFarmFavoritesTable(db)
        createFarmPhotosTable(db)
        createCropBoundariesTable(db)
    }

    private fun createCropBoundariesTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS crop_boundaries (
                cropId INTEGER PRIMARY KEY,
                points TEXT NOT NULL,
                FOREIGN KEY(cropId)
                REFERENCES crops(cropId) ON DELETE CASCADE
            )
        """.trimIndent())
    }

    private fun createUsersTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fullName TEXT NOT NULL,
                email TEXT NOT NULL COLLATE NOCASE UNIQUE,
                passwordHash TEXT NOT NULL,
                createdAt TEXT NOT NULL
            )
        """.trimIndent())
    }

    private fun createFarmFavoritesTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS farm_favorites (
                farmId INTEGER PRIMARY KEY,
                FOREIGN KEY(farmId)
                REFERENCES farms(id) ON DELETE CASCADE
            )
        """.trimIndent())
    }

    private fun createFarmPhotosTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS farm_photos (
                photoId INTEGER PRIMARY KEY AUTOINCREMENT,
                farmId INTEGER NOT NULL,
                photoUri TEXT NOT NULL,
                FOREIGN KEY(farmId)
                REFERENCES farms(id) ON DELETE CASCADE
            )
        """.trimIndent())
    }

    // Factory methods return INTERFACES (Dependency Inversion):
    // callers never need to know the SQLite classes.
    fun farmDao(): FarmRepository = FarmDao(this)

    fun cropDao(): CropRepository = CropDao(this)

    fun harvestDao(): HarvestRepository = HarvestDao(this)

    fun productionAreaDao(): ProductionAreaRepository =
        ProductionAreaDao(this)

    fun businessDao(): BusinessRepository = FarmBusinessDao(this)

    fun boundaryDao(): BoundaryRepository = BoundaryDao(this)

    fun favoriteDao(): FavoriteRepository = FavoriteDao(this)

    fun farmPhotoDao(): FarmPhotoRepository = FarmPhotoDao(this)

    fun userDao(): UserDataSource = UserDao(this)

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
