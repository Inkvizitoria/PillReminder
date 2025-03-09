package com.health.pillreminder.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.model.DayCell
import java.text.SimpleDateFormat
import java.util.*

class MonthGridNoOffsetAdapter(
    private val dayCells: List<DayCell>,
    private val onDayClick: (DayCell) -> Unit
) : RecyclerView.Adapter<MonthGridNoOffsetAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_cell_no_offset, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val cell = dayCells[position]
        holder.bind(cell)
        holder.itemView.setOnClickListener { onDayClick(cell) }
    }

    override fun getItemCount(): Int = dayCells.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayNumber: TextView = itemView.findViewById(R.id.tvDayNumber)
        private val tvDayOfWeek: TextView = itemView.findViewById(R.id.tvDayOfWeek)
        private val dayEventContainer: LinearLayout = itemView.findViewById(R.id.dayEventContainer)

        @SuppressLint("DefaultLocale", "DiscouragedApi")
        fun bind(cell: DayCell) {
            tvDayNumber.text = cell.day.toString()
            tvDayOfWeek.text = cell.dayOfWeek
            dayEventContainer.removeAllViews()

            for (entry in cell.entries) {
                if (!entry.isDeleted) {
                    val blockView = TextView(dayEventContainer.context).apply {
                        textSize = 12f
                        setPadding(16, 4, 8, 4)
                        text = if (entry.repeatValue != null && entry.repeatUnit != null) {
                            "${entry.name}"
                        } else {
                            val formattedDate = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entry.scheduledTime))
                            "${entry.name} в $formattedDate"
                        }
                        try {
                            val context = dayEventContainer.context
                            // Используем ключ blockColor напрямую:
                            val drawableName = "item_${entry.blockColor}"
                            val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                            if (resId != 0) {
                                setBackgroundResource(resId)
                            } else {
                                setBackgroundColor(Color.LTGRAY)
                            }
                        } catch (e: Exception) {
                            setBackgroundColor(Color.LTGRAY)
                        }
                        // Автоматически подбираем цвет текста для контраста
                        val hexColor = colorMapping[entry.blockColor] ?: "#FFFFFF"
                        setTextColor(getContrastingTextColor(hexColor))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(4, 4, 4, 4)
                        }
                    }
                    dayEventContainer.addView(blockView)
                }
            }
        }
    }

    private val colorMapping = mapOf(
        "red_light" to "#FFCDD2",
        "red" to "#F44336",
        "red_dark" to "#C2185B",
        "red_extra" to "#B71C1C",
        "orange_light" to "#FFE0B2",
        "orange" to "#FF9800",
        "orange_dark" to "#F57C00",
        "orange_extra" to "#E65100",
        "yellow_light" to "#FFF9C4",
        "yellow" to "#FFEB3B",
        "yellow_dark" to "#FBC02D",
        "yellow_extra" to "#F57F17",
        "green_light" to "#C8E6C9",
        "green" to "#4CAF50",
        "green_dark" to "#388E3C",
        "green_extra" to "#1B5E20",
        "blue_light" to "#BBDEFB",
        "blue" to "#2196F3",
        "blue_dark" to "#1976D2",
        "blue_extra" to "#0D47A1",
        "indigo_light" to "#C5CAE9",
        "indigo" to "#3F51B5",
        "indigo_dark" to "#303F9F",
        "indigo_extra" to "#1A237E",
        "violet_light" to "#E1BEE7",
        "violet" to "#9C27B0",
        "violet_dark" to "#7B1FA2",
        "violet_extra" to "#4A148C",
        "pink_light" to "#F8BBD0",
        "pink" to "#E91E63",
        "pink_dark" to "#C2185B",
        "cyan" to "#00BCD4",
        "cyan_dark" to "#0097A7",
        "lime" to "#CDDC39",
        "lime_dark" to "#AFB42B",
        "brown_light" to "#D7CCC8",
        "brown" to "#795548",
        "brown_dark" to "#5D4037",
        "brown_extra" to "#3E2723"
    )

    private fun getContrastingTextColor(hexColor: String): Int {
        return try {
            val color = Color.parseColor(hexColor)
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
            if (luminance > 0.5) Color.BLACK else Color.WHITE
        } catch (e: Exception) {
            Color.BLACK
        }
    }
}
