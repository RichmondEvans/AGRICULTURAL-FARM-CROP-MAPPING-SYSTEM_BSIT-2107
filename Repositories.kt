package com.example.agricultural

/*
 * INTERFACE SEGREGATION + DEPENDENCY INVERSION
 *
 * Screens and services depend on these small interfaces, never on
 * SQLite classes. Any storage (SQLite, Room, a fake for tests) can
 * be plugged in without changing the screens.
 */

interface ReadRepository<T> {
    suspend fun getById(id: Int): T?
    suspend fun getAll(): List<T>
}

interface WriteRepository<T> {
    suspend fun insert(item: T): Long
    suspend fun update(item: T): Int
    suspend fun delete(id: Int): Int
}

/** Records that belong to one farm (crops, areas, harvests, business). */
interface FarmScopedRepository<T : AgriculturalRecord> :
    ReadRepository<T>, WriteRepository<T> {
    suspend fun getByFarmId(farmId: Int): List<T>
}

interface CropRepository : FarmScopedRepository<Crop>

interface ProductionAreaRepository : FarmScopedRepository<ProductionArea>

interface HarvestRepository : FarmScopedRepository<Harvest>

interface BusinessRepository : FarmScopedRepository<FarmBusiness>

interface FarmRepository : ReadRepository<Farm>, WriteRepository<Farm> {
    suspend fun search(keyword: String): List<Farm>
    suspend fun count(): Int
    suspend fun updateFarmPhoto(farmId: Int, photoUri: String): Int

    // Recycle bin (soft delete)
    suspend fun getTrashFarms(): List<Farm>
    suspend fun moveToTrash(farmId: Int): Int
    suspend fun restoreFarm(farmId: Int): Int
}

interface BoundaryRepository {
    suspend fun save(cropId: Int, points: List<Pair<Double, Double>>)
    suspend fun get(cropId: Int): List<Pair<Double, Double>>
    suspend fun delete(cropId: Int): Int
    suspend fun getForFarm(farmId: Int): List<CropBoundary>
}

interface FavoriteRepository {
    suspend fun save(farmId: Int)
    suspend fun remove(farmId: Int): Int
    suspend fun isFavorite(farmId: Int): Boolean
    suspend fun getFavoriteFarms(): List<Farm>
}

interface FarmPhotoRepository {
    suspend fun add(farmId: Int, photoUri: String): Long
    suspend fun getByFarm(farmId: Int): List<String>
}
