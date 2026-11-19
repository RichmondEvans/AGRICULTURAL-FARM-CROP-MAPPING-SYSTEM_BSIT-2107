package com.example.agricultural

// Summary of one farm.
data class FarmReport(
    val farm: Farm,
    val cropCount: Int,
    val areaCount: Int,
    val totalHarvestQuantity: Double,
    val finance: FinancialSummary
)

// Summary of all registered farms.
data class AgriculturalReport(
    val farms: List<FarmReport>
) {
    val farmCount: Int get() = farms.size
    val cropCount: Int get() = farms.sumOf { it.cropCount }
    val areaCount: Int get() = farms.sumOf { it.areaCount }
    val totalHarvestQuantity: Double get() = farms.sumOf { it.totalHarvestQuantity }
    val finance: FinancialSummary
        get() = farms.fold(FinancialSummary.EMPTY) { total, f -> total + f.finance }
}

class ReportService(
    private val farms: FarmRepository,
    private val crops: CropRepository,
    private val areas: ProductionAreaRepository,
    private val harvests: HarvestRepository,
    private val business: BusinessRepository
) {

    suspend fun generate(): AgriculturalReport =
        AgriculturalReport(
            farms.getAll().map { farm ->
                FarmReport(
                    farm = farm,
                    cropCount = crops.getByFarmId(farm.id).size,
                    areaCount = areas.getByFarmId(farm.id).size,
                    totalHarvestQuantity = harvests.getByFarmId(farm.id)
                        .sumOf { it.availableQuantity },
                    finance = FinancialSummary.of(business.getByFarmId(farm.id))
                )
            }
        )
}
