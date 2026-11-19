package com.example.agricultural


interface ReadRepository<T> {
    suspend fun getById(id: Int): T?
    suspend fun getAll(): List<T>
}

interface WriteRepository<T> {
    suspend fun insert(item: T): Long
    suspend fun update(item: T): Int
    suspend fun delete(id: Int): Int
}

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
