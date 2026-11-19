package com.example.agricultural

import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import com.example.agricultural.databinding.ActivityProductionAreaBinding

// Production Areas screen
class ProductionAreaActivity :
    RecordCrudActivity<ProductionArea, ActivityProductionAreaBinding>() {

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityProductionAreaBinding.inflate(inflater)

    override val repository: ProductionAreaRepository by lazy {
        database.productionAreaDao()
    }

    override val backButton: Button get() = binding.btnBack
    override val saveButton: Button get() = binding.btnSaveArea
    override val listContainer: LinearLayout get() = binding.layoutAreaList

    override val addLabel = "＋ Save Area"
    override val updateLabel = "Update Area"
    override val editLabel = "Edit Area"
    override val deleteLabel = "Delete Area"
    override val emptyMessage = "No production areas registered yet."
    override val savedMessage = "Production area saved!"
    override val deletedMessage = "Area deleted"

    override fun readForm(recordId: Int): ProductionArea? {
        val unit = binding.etAreaUnit.text.toString().trim()
        val soil = binding.etSoilType.text.toString().trim()
        val status = binding.etLandStatus.text.toString().trim()

        if (unit.isBlank() || soil.isBlank() || status.isBlank()) {
            toast("Please fill all fields")
            return null
        }

        return ProductionArea(
            id = recordId,
            farmId = farmId,
            areaUnit = unit,
            soilType = soil,
            currentLandStatus = status
        )
    }

    override fun fillForm(record: ProductionArea) {
        binding.etAreaUnit.setText(record.areaUnit)
        binding.etSoilType.setText(record.soilType)
        binding.etLandStatus.setText(record.currentLandStatus)
    }

    override fun clearForm() {
        binding.etAreaUnit.text.clear()
        binding.etSoilType.text.clear()
        binding.etLandStatus.text.clear()
    }

    override fun cardLines(record: ProductionArea) = listOf(
        CardLine.normal("Area: ${record.areaUnit}"),
        CardLine.normal("Soil Type: ${record.soilType}"),
        CardLine.normal("Land Status: ${record.currentLandStatus}")
    )
}
