package com.example.agricultural

/**
 * SINGLE RESPONSIBILITY: puts the sample Nasugbu farms, crops, harvests
 * and production areas in the database. It was inside MainActivity
 * (600+ lines); the screen now only shows data.
 *
 * It works with the repository INTERFACES, so it does not care
 * how the data is stored (Dependency Inversion).
 */
class SampleDataSeeder(
    private val farmRepository: FarmRepository,
    private val cropRepository: CropRepository,
    private val harvestRepository: HarvestRepository,
    private val areaRepository: ProductionAreaRepository
) {

    /** Adds the missing sample data. Returns how many new farms were added. */
    suspend fun seed(): Int {
        val added = seedFarms()
        seedAgriculturalData()
        return added
    }

    private suspend fun seedFarms(): Int {
        val existingNames = farmRepository.getAll()
            .map { it.farmName.trim().lowercase() }
            .toMutableSet()

        var added = 0

        for (farm in farms) {
            val name = farm.farmName.trim().lowercase()

            if (name !in existingNames) {
                farmRepository.insert(farm)
                existingNames.add(name)
                added++
            }
        }

        return added
    }

    private suspend fun seedAgriculturalData() {
        val existingFarmIds = farmRepository.getAll().map { it.id }.toSet()

        for (crop in crops) {
            if (crop.farmId !in existingFarmIds) continue
            if (cropRepository.getById(crop.id) == null) {
                cropRepository.insert(crop)
            }
        }

        for (area in areas) {
            if (area.farmId !in existingFarmIds) continue
            if (areaRepository.getById(area.id) == null) {
                areaRepository.insert(area)
            }
        }

        // A harvest belongs to a crop, so skip it if the crop is missing.
        for (seed in harvests) {
            val crop = cropRepository.getById(seed.cropId) ?: continue

            if (harvestRepository.getById(seed.id) == null) {
                harvestRepository.insert(
                    Harvest(
                        id = seed.id,
                        farmId = crop.farmId,
                        cropId = seed.cropId,
                        harvestDate = seed.date,
                        harvestQuality = seed.quality,
                        availableQuantity = seed.quantity
                    )
                )
            }
        }
    }

    private class HarvestSeed(
        val id: Int,
        val cropId: Int,
        val date: String,
        val quality: String,
        val quantity: Double
    )

    private val crops = listOf(
        Crop(1, 1, "Melon", "Honeydew", "Ready for Harvest"),
        Crop(2, 1, "Chili", "Siling Labuyo", "Growing"),
        Crop(3, 2, "Mushroom", "White Oyster Mushroom", "Ready for Harvest"),
        Crop(4, 3, "Papaya", "Red Lady", "Ready for Harvest"),
        Crop(5, 3, "Corn", "Sweet Golden Corn", "Growing"),
        Crop(6, 3, "Cucumber", "Japanese Cucumber", "Ready for Harvest"),
        Crop(7, 4, "Dragon Fruit", "Red Pitaya", "Flowering"),
        Crop(8, 6, "Sugarcane", "Phil 2006-2289", "Growing"),
        Crop(9, 7, "Coconut", "Laguna Tall", "Ready for Harvest"),
        Crop(10, 11, "Radish", "White Icicle", "Ready for Harvest"),
        Crop(11, 17, "Lettuce", "Romaine Hydroponic", "Ready for Harvest"),
        Crop(12, 18, "Coffee", "Robusta", "Growing"),
        Crop(13, 18, "Black Pepper", "Panniyur-1", "Ready for Harvest"),
        Crop(14, 20, "Tomato", "Cherry Tomato", "Ready for Harvest")
    )

    private val harvests = listOf(
        HarvestSeed(1, 1, "2026-11-15", "Grade A", 250.5),
        HarvestSeed(2, 3, "2026-11-17", "Premium Fresh", 85.0),
        HarvestSeed(3, 4, "2026-11-14", "Grade A", 400.0),
        HarvestSeed(4, 6, "2026-11-16", "Grade A", 150.0),
        HarvestSeed(5, 9, "2026-11-10", "Standard", 1200.0),
        HarvestSeed(6, 10, "2026-11-16", "Grade A", 95.5),
        HarvestSeed(7, 11, "2026-11-17", "Organic Grade A", 60.0),
        HarvestSeed(8, 13, "2026-11-12", "Dried Whole", 110.0),
        HarvestSeed(9, 14, "2026-11-17", "Organic Grade A", 45.0)
    )

    private val areas = listOf(
        ProductionArea(1, 1, "3.5 Hectares", "Loam", "Cultivated - Drip Irrigated"),
        ProductionArea(2, 2, "500 Sq. Meters", "Indoor Growing House", "Active Production"),
        ProductionArea(3, 3, "5.0 Hectares", "Sandy Loam", "Cultivated - Organic"),
        ProductionArea(4, 4, "2.0 Hectares", "Clay Loam", "Cultivated"),
        ProductionArea(5, 6, "4.2 Hectares", "Silty Clay", "Cultivated"),
        ProductionArea(6, 7, "6.0 Hectares", "Sandy Clay Loam", "Cultivated"),
        ProductionArea(7, 11, "1.5 Hectares", "Loam", "Cultivated"),
        ProductionArea(8, 17, "4.0 Hectares", "Loam / Hydroponic", "Active Agri-Tourism"),
        ProductionArea(9, 18, "8.0 Hectares", "Volcanic Loam", "Agroforestry"),
        ProductionArea(10, 20, "2.5 Hectares", "Loam", "Cultivated - Organic")
    )

    private val farms = listOf(

        Farm(
            farmName = "B2GO Farms",
            ownerName = "Not publicly listed",
            farmAddress = "Sitio Kaybibisaya, Brgy. Aga, Nasugbu, Batangas",
            contactNumber = "09157473483",
            latitude = 14.0886,
            longitude = 120.7712
        ),

        Farm(
            farmName = "3G Country Farms",
            ownerName = "Not publicly listed",
            farmAddress = "Sitio Kaybibisaya, Brgy. Aga, Nasugbu, Batangas",
            contactNumber = "09669519334",
            latitude = 14.0894,
            longitude = 120.7725
        ),

        Farm(
            farmName = "Sansar Eco Farm Inc.",
            ownerName = "Not publicly listed",
            farmAddress = "Brgy. Aga, Nasugbu, Batangas",
            contactNumber = "09178739665",
            latitude = 14.0915,
            longitude = 120.7785
        ),

        Farm(
            farmName = "Oni Agrifarm Inc.",
            ownerName = "Not publicly listed",
            farmAddress = "Sitio Balite, Brgy. Banilad, Nasugbu, Batangas",
            contactNumber = "09663898830",
            latitude = 14.0730,
            longitude = 120.7408
        ),

        Farm(
            farmName = "Sunmegaprime Pitaya Farms",
            ownerName = "Not publicly listed",
            farmAddress = "Brgy. Catandaan, Nasugbu, Batangas",
            contactNumber = "09175200107",
            latitude = 14.0804,
            longitude = 120.6815
        ),

        Farm(
            farmName = "Graceland Dragonfruit Garden",
            ownerName = "Not publicly listed",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "09158980922",
            latitude = 14.0765,
            longitude = 120.6540
        ),

        Farm(
            farmName = "3A Farm",
            ownerName = "Not publicly listed",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "09175391151",
            latitude = 14.0835,
            longitude = 120.6680
        ),

        Farm(
            farmName = "Talamante Farm",
            ownerName = "Not publicly listed",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "09955102405",
            latitude = 14.0695,
            longitude = 120.6610
        ),

        Farm(
            farmName = "TDR Farms",
            ownerName = "Not publicly listed",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "",
            latitude = 14.0750,
            longitude = 120.6720
        ),

        Farm(
            farmName = "Sissy Farm",
            ownerName = "Not publicly listed",
            farmAddress = "Reparo, Nasugbu, Batangas",
            contactNumber = "09358226572",
            latitude = 14.0728,
            longitude = 120.6923
        ),

        Farm(
            farmName = "VFNR Agricultural Farm",
            ownerName = "Not publicly listed",
            farmAddress = "Brgy. Tumalim, Nasugbu, Batangas",
            contactNumber = "09174347918",
            latitude = 14.0786,
            longitude = 120.7224
        ),

        Farm(
            farmName = "B2GO Farms",
            ownerName = "B2GO Management",
            farmAddress = "Brgy. Aga, Nasugbu, Batangas",
            contactNumber = "0960 227 5960",
            latitude = 14.0845,
            longitude = 120.7612,
            website = "https://b2gofarms.com"
        ),

        Farm(
            farmName = "3G Country Farms",
            ownerName = "3G Management",
            farmAddress = "Sitio Kaybibisaya, Brgy. Aga, Nasugbu, Batangas",
            contactNumber = "0929 535 6190",
            latitude = 14.0812,
            longitude = 120.7580
        ),

        Farm(
            farmName = "Farmer Juan Ranch",
            ownerName = "Juan Dela Cruz",
            farmAddress = "Sitio Pari, Nasugbu, Batangas",
            contactNumber = "0917 859 2406",
            latitude = 14.0725,
            longitude = 120.6640,
            website = "https://farmerjuanranch.com"
        ),

        Farm(
            farmName = "SUNMEGAPRIME PITAYA FARMS",
            ownerName = "Sunmegaprime Management",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0750,
            longitude = 120.6550
        ),

        Farm(
            farmName = "The Olive Moon Farm",
            ownerName = "Olive Moon Management",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0790,
            longitude = 120.6810
        ),

        Farm(
            farmName = "Santos Farm",
            ownerName = "Pamilya Santos",
            farmAddress = "Sitio Kaytuyang, Brgy. Aga, Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0860,
            longitude = 120.7650
        ),

        Farm(
            farmName = "Mabasima Farms",
            ownerName = "Mabasima Management",
            farmAddress = "Brgy. Mataas na Pulo, Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.1250,
            longitude = 120.6420
        ),

        Farm(
            farmName = "Belisario Farm",
            ownerName = "Pamilya Belisario",
            farmAddress = "Brgy. Banilad, Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0680,
            longitude = 120.7120
        ),

        Farm(
            farmName = "VFNR Agricultural Farm",
            ownerName = "VFNR Management",
            farmAddress = "Sitio Boulevard, Brgy. Tumalim, Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0610,
            longitude = 120.7280
        ),

        Farm(
            farmName = "Talamante Farm",
            ownerName = "Pamilya Talamante",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0920,
            longitude = 120.7410
        ),

        Farm(
            farmName = "The Radish Farm",
            ownerName = "Radish Farm Management",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0830,
            longitude = 120.7350
        ),

        Farm(
            farmName = "Ana's Farm",
            ownerName = "Ana",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0765,
            longitude = 120.6920
        ),

        Farm(
            farmName = "FARM NI BECKY",
            ownerName = "Becky",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0740,
            longitude = 120.6880
        ),

        Farm(
            farmName = "MSC Farm",
            ownerName = "MSC Management",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0710,
            longitude = 120.6750
        ),

        Farm(
            farmName = "G.V. Farms / GVL Tumalim Farms",
            ownerName = "GVL Management",
            farmAddress = "Brgy. Tumalim, Nasugbu, Batangas",
            contactNumber = "0977 803 7776",
            latitude = 14.0595,
            longitude = 120.7310
        ),

        Farm(
            farmName = "Mijas Farms",
            ownerName = "Mijas Management",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0650,
            longitude = 120.6980
        ),

        Farm(
            farmName = "Harvest Hills Family Farm",
            ownerName = "Harvest Hills Management",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0890,
            longitude = 120.7520
        ),

        Farm(
            farmName = "Bamboo Hills Leisure Farm",
            ownerName = "Bamboo Hills Management",
            farmAddress = "Brgy. Kayrilaw, Nasugbu, Batangas",
            contactNumber = "0917 854 7458",
            latitude = 14.1150,
            longitude = 120.7780
        ),

        Farm(
            farmName = "3A Farm",
            ownerName = "3A Management",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "0917 509 7376",
            latitude = 14.0815,
            longitude = 120.7480,
            website = "https://3afarm.com"
        ),

        Farm(
            farmName = "Casitas de Victoria (Lolo Doc Farm)",
            ownerName = "Victoria Management",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0785,
            longitude = 120.6690
        ),

        Farm(
            farmName = "Tamaraw Farm & Resort",
            ownerName = "Tamaraw Management",
            farmAddress = "Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0730,
            longitude = 120.6590
        ),

        Farm(
            farmName = "Farm Villa",
            ownerName = "Farm Villa Management",
            farmAddress = "Sitio Hulo, Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0805,
            longitude = 120.7150
        ),

        Farm(
            farmName = "Mountain View Leisure Community",
            ownerName = "Mountain View Developers",
            farmAddress = "Brgy. Munting Indang, Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.1320,
            longitude = 120.7180
        ),

        Farm(
            farmName = "Mandaragat Agape's Farm",
            ownerName = "Mandaragat Family",
            farmAddress = "Sitio Anahaw, Brgy. Maugat, Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0950,
            longitude = 120.6850
        ),

        Farm(
            farmName = "Sitio de Oro Nature’s Farm",
            ownerName = "Sitio de Oro Management",
            farmAddress = "Brgy. Malapad na Bato, Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.1020,
            longitude = 120.7250
        ),

        Farm(
            farmName = "Camp and Farm!",
            ownerName = "Camp and Farm Management",
            farmAddress = "Tagaytay-Nasugbu Highway, Nasugbu, Batangas",
            contactNumber = "N/A",
            latitude = 14.0875,
            longitude = 120.7680
        )
    )
}
