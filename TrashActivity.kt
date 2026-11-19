package com.example.agricultural

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class TrashActivity : AppCompatActivity() {

    private val dao by lazy {
        AppDatabase.get(this).farmDao()
    }

    private lateinit var list: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Recycle Bin"
            textSize = 26f
            setTypeface(null, Typeface.BOLD)
        }

        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val back = Button(this).apply {
            text = "Back"
            setOnClickListener { finish() }
        }

        root.addView(title)
        root.addView(list)
        root.addView(back)

        setContentView(ScrollView(this).apply { addView(root) })

        loadTrash()
    }

    private fun loadTrash() {
        lifecycleScope.launch {
            val farms = dao.getTrashFarms()
            list.removeAllViews()

            if (farms.isEmpty()) {
                val empty = TextView(this@TrashActivity)
                empty.text = "Walang laman ang Recycle Bin."
                empty.setPadding(0, 40, 0, 40)
                list.addView(empty)
            }

            farms.forEach { farm ->
                list.addView(makeCard(farm))
            }
        }
    }

    private fun makeCard(farm: Farm): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(20, 18, 20, 18)
        card.setBackgroundColor(Color.rgb(240, 248, 240))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 8, 0, 12)
        card.layoutParams = params

        val name = TextView(this)
        name.text = farm.farmName
        name.textSize = 19f
        name.setTypeface(null, Typeface.BOLD)
        card.addView(name)

        val address = TextView(this)
        address.text = farm.farmAddress
        address.setPadding(0, 6, 0, 12)
        card.addView(address)

        val restore = Button(this)
        restore.text = "Restore"
        restore.setOnClickListener {
            lifecycleScope.launch {
                dao.restoreFarm(farm.id)
                Toast.makeText(
                    this@TrashActivity,
                    "Farm restored",
                    Toast.LENGTH_SHORT
                ).show()
                loadTrash()
            }
        }
        card.addView(restore)

        val deleteForever = Button(this)
        deleteForever.text = "Delete Forever"
        deleteForever.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Forever")
                .setMessage("Mabubura nang tuluyan ang farm at lahat ng records nito. Hindi na ito maibabalik.")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            dao.delete(farm.id)
                            loadTrash()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@TrashActivity,
                                "Delete failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        card.addView(deleteForever)

        return card
    }
}