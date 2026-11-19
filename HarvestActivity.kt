package com.example.agricultural

import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import com.example.agricultural.databinding.ActivityHarvestBinding

// Harvest Records screen.
class HarvestActivity :
    RecordCrudActivity<Harvest, ActivityHarvestBinding>() {

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityHarvestBinding.inflate(inflater)

    override val repository: HarvestRepository by lazy { database.harvestDao() }

    private val cropRepository: CropRepository by lazy { database.cropDao() }

    // cropId -> crop name, used to show a readable name on each card
    private var cropNames: Map<Int, String> = emptyMap()

    override val backButton: Button get() = binding.btnBack
    override val saveButton: Button get() = binding.btnSaveHarvest
    override val listContainer: LinearLayout get() = binding.layoutHarvestList

    override val addLabel = "＋ Save Harvest"
    override val updateLabel = "Update Harvest"
    override val editLabel = "Edit Harvest"
    override val deleteLabel = "Delete Harvest"
    override val emptyMessage = "No harvest records available."
    override val savedMessage = "Harvest saved successfully!"
    override val deletedMessage = "Harvest deleted"

    override fun readForm(recordId: Int): Harvest? {
        val cropId = binding.etCropId.text.toString().trim().toIntOrNull()
        val date = binding.etHarvestDate.text.toString().trim()
        val quality = binding.etHarvestQuality.text.toString().trim()
        val quantity = binding.etAvailableQuantity.text.toString()
            .trim().toDoubleOrNull()

        if (
            cropId == null ||
            date.isBlank() ||
            quality.isBlank() ||
            quantity == null ||
            quantity < 0
        ) {
            toast("Please enter valid information")
            return null
        }

        return Harvest(
            id = recordId,
            farmId = farmId,
            cropId = cropId,
            harvestDate = date,
            harvestQuality = quality,
            availableQuantity = quantity
        )
    }

    // A harvest may only point to a crop of THIS farm.
    override suspend fun validate(record: Harvest): String? {
        val belongs = cropRepository.getByFarmId(farmId)
            .any { it.id == record.cropId }

        return if (belongs) null else "Crop ID does not belong to this farm"
    }

    override suspend fun loadRecords(): List<Harvest> {
        cropNames = cropRepository.getByFarmId(farmId)
            .associate { it.id to it.cropName }

        return super.loadRecords()
    }

    override fun fillForm(record: Harvest) {
        binding.etCropId.setText(record.cropId.toString())
        binding.etHarvestDate.setText(record.harvestDate)
        binding.etHarvestQuality.setText(record.harvestQuality)
        binding.etAvailableQuantity.setText(record.availableQuantity.toString())
    }

    override fun clearForm() {
        binding.etCropId.text.clear()
        binding.etHarvestDate.text.clear()
        binding.etHarvestQuality.text.clear()
        binding.etAvailableQuantity.text.clear()
    }

    override fun cardLines(record: Harvest): List<CardLine> {
        val cropName = cropNames[record.cropId]
        val crop = if (cropName != null) "$cropName (ID ${record.cropId})"
        else "ID ${record.cropId}"

        return listOf(
            CardLine.normal("Crop: $crop"),
            CardLine.normal("Harvest Date: ${record.harvestDate}"),
            CardLine.normal("Quality: ${record.harvestQuality}"),
            CardLine.highlight("Available Quantity: ${record.availableQuantity}")
        )
    }
}
