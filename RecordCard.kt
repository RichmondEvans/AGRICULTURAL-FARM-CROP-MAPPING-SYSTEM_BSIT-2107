package com.example.agricultural

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** One line of text inside a record card. */
data class CardLine(
    val text: String,
    val bold: Boolean = false,
    val color: Int = Color.DKGRAY,
    val textSize: Float = 15f
) {
    companion object {
        fun normal(text: String) = CardLine(text)

        fun highlight(text: String) =
            CardLine(text, bold = true, color = GREEN)

        fun signed(text: String, positive: Boolean) = CardLine(
            text,
            bold = true,
            color = if (positive) POSITIVE else Color.RED,
            textSize = 17f
        )

        val GREEN = Color.rgb(35, 100, 55)
        val POSITIVE = Color.rgb(35, 130, 65)
    }
}

/**
 * SINGLE RESPONSIBILITY: builds the card (title, lines, Edit and
 * Delete buttons) used by the Crops, Areas, Harvest and Business screens.
 */
class RecordCardBuilder(private val context: Context) {

    fun build(
        title: String,
        lines: List<CardLine>,
        editLabel: String,
        deleteLabel: String,
        onEdit: () -> Unit,
        onDelete: () -> Unit
    ): View {
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            setBackgroundColor(Color.rgb(240, 248, 240))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 12) }
        }

        card.addView(
            TextView(context).apply {
                text = title
                textSize = 19f
                setTypeface(null, Typeface.BOLD)
                setTextColor(CardLine.GREEN)
            }
        )

        lines.forEachIndexed { index, line ->
            card.addView(
                TextView(context).apply {
                    text = line.text
                    textSize = line.textSize
                    setTextColor(line.color)
                    if (line.bold) setTypeface(null, Typeface.BOLD)

                    val top = if (index == 0) 8 else 5
                    val bottom = if (index == lines.lastIndex) 12 else 0
                    setPadding(0, top, 0, bottom)
                }
            )
        }

        card.addView(Button(context).apply {
            text = editLabel
            setOnClickListener { onEdit() }
        })

        card.addView(Button(context).apply {
            text = deleteLabel
            setOnClickListener { onDelete() }
        })

        return card
    }
}
