package com.example.agricultural

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewbinding.ViewBinding

abstract class RecordCrudActivity<T : AgriculturalRecord, B : ViewBinding> :
    FarmScopedActivity() {

    protected lateinit var binding: B
        private set

    private var editingId = -1

    private val cardBuilder by lazy { RecordCardBuilder(this) }

    protected abstract fun inflateBinding(inflater: LayoutInflater): B

    protected abstract val repository: FarmScopedRepository<T>
    protected abstract val backButton: Button
    protected abstract val saveButton: Button
    protected abstract val listContainer: LinearLayout

    protected abstract val addLabel: String
    protected abstract val updateLabel: String
    protected abstract val editLabel: String
    protected abstract val deleteLabel: String
    protected abstract val emptyMessage: String
    protected abstract val savedMessage: String
    protected abstract val deletedMessage: String

    /** Build the record from the form, or show a message and return null. */
    protected abstract fun readForm(recordId: Int): T?

    protected abstract fun fillForm(record: T)

    protected abstract fun clearForm()

    protected abstract fun cardLines(record: T): List<CardLine>

    // ---------- optional hooks (children may override) ----------

    protected open fun cardTitle(record: T): String = record.displayTitle

    /** Return an error message to stop the save, or null when valid. */
    protected open suspend fun validate(record: T): String? = null

    protected open suspend fun loadRecords(): List<T> =
        repository.getByFarmId(farmId)

    protected open fun onRecordsLoaded(records: List<T>) {}

    // ---------- the fixed flow ----------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)

        if (!resolveFarmId()) return

        backButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { save() }
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized && hasValidFarm()) refresh()
    }

    private fun save() {
        val record = readForm(if (editingId > 0) editingId else 0) ?: return

        launchSafely("Save failed") {
            val problem = validate(record)

            if (problem != null) {
                toast(problem)
                return@launchSafely
            }

            if (editingId > 0) {
                repository.update(record)
            } else {
                repository.insert(record)
            }

            resetForm()
            refresh()
            toast(savedMessage)
        }
    }

    private fun delete(record: T) {
        launchSafely("Delete failed") {
            repository.delete(record.id)

            if (editingId == record.id) resetForm()

            refresh()
            toast(deletedMessage)
        }
    }

    private fun startEditing(record: T) {
        editingId = record.id
        fillForm(record)
        saveButton.text = updateLabel
    }

    private fun resetForm() {
        editingId = -1
        clearForm()
        saveButton.text = addLabel
    }

    private fun refresh() {
        launchSafely("Loading failed") {
            val records = loadRecords()

            listContainer.removeAllViews()

            if (records.isEmpty()) {
                listContainer.addView(emptyView())
            }

            records.forEach { record ->
                listContainer.addView(
                    cardBuilder.build(
                        title = cardTitle(record),
                        lines = cardLines(record),
                        editLabel = editLabel,
                        deleteLabel = deleteLabel,
                        onEdit = { startEditing(record) },
                        onDelete = { delete(record) }
                    )
                )
            }

            onRecordsLoaded(records)
        }
    }

    private fun emptyView() = TextView(this).apply {
        text = emptyMessage
        textSize = 15f
        gravity = Gravity.CENTER
        setTextColor(Color.GRAY)
        setPadding(20, 35, 20, 35)
    }
}
