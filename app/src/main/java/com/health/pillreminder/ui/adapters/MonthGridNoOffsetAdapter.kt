package com.health.pillreminder.ui.fragments

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.model.DayCell
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

        @SuppressLint("DefaultLocale")
        fun bind(cell: DayCell) {
            tvDayNumber.text = cell.day.toString()
            tvDayOfWeek.text = cell.dayOfWeek
            dayEventContainer.removeAllViews()

            // Для каждого entry создаём TextView с отступами
            for (entry in cell.entries) {
                if (!entry.isDeleted) {
                    val blockView = TextView(dayEventContainer.context).apply {
                        textSize = 12f
                        setPadding(8, 4, 8, 4)
                        text = if (entry.repeatValue != null && entry.repeatUnit != null) {
                            "${entry.name} Каждые ${entry.repeatValue} ${entry.repeatUnit}"
                        } else {
                            val formattedDate = SimpleDateFormat(
                                "HH:mm",
                                Locale.getDefault()
                            ).format(Date(entry.scheduledTime))
                            "${entry.name} в $formattedDate"
                        }
                        setBackgroundResource(R.drawable.event_block_background)

                        // Добавляем отступы между блоками
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(4, 4, 4, 4) // Отступы сверху и снизу
                        }
                    }
                    dayEventContainer.addView(blockView)
                }
            }
        }
    }


}


