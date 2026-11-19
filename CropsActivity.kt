package com.example.agricultural

import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import com.example.agricultural.databinding.ActivityCropsBinding

/** Crops Information screen. Only the crop-specific parts are written here. */
class CropsActivity : RecordCrudActivity<Crop, ActivityCropsBinding>() {

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityCropsBinding.inflate(inflater)

    override val repository: CropRepository by lazy { database.cropDao() }

    override val backButton: Button get() = binding.btnBack
    override val saveButton: Button get() = binding.btnSaveCrop
    override val listContainer: LinearLayout get() = binding.layoutCropList

    override val addLabel = "＋ Save Crop"
    override val updateLabel = "Update Crop"
    override val editLabel = "Edit Crop"
    override val deleteLabel = "Delete Crop"
    override val emptyMessage = "No crops registered yet."
    override val savedMessage = "Crop saved successfully!"
    override val deletedMessage = "Crop deleted"

    override fun readForm(recordId: Int): Crop? {
        val name = binding.etCropName.text.toString().trim()
        val variety = binding.etCropVariety.text.toString().trim()
        val status = binding.etCropStatus.text.toString().trim()

        if (name.isBlank() || variety.isBlank() || status.isBlank()) {
            toast("Please fill all fields")
            return null
        }

        return Crop(
            id = recordId,
            farmId = farmId,
            cropName = name,
            cropVariety = variety,
            cropStatus = status
        )
    }

    override fun fillForm(record: Crop) {
        binding.etCropName.setText(record.cropName)
        binding.etCropVariety.setText(record.cropVariety)
        binding.etCropStatus.setText(record.cropStatus)
    }

    override fun clearForm() {
        binding.etCropName.text.clear()
        binding.etCropVariety.text.clear()
        binding.etCropStatus.text.clear()
    }

    override fun cardLines(record: Crop) = listOf(
        CardLine.normal("Variety: ${record.cropVariety}"),
        CardLine.normal("Status: ${record.cropStatus}")
    )
}
