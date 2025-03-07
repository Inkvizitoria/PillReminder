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

            // Для каждого entry в cell.entries создаём небольшой прямоугольник или TextView
            for (entry in cell.entries) {
                val blockView = TextView(dayEventContainer.context).apply {
                    textSize = 12f
                    setPadding(4, 2, 4, 2)
                    // Пример: выводим "R4мин" если repeatValue=4, repeatUnit=Минут
                    // или время scheduledTime, если фиксированная
                    text = if (entry.repeatValue != null && entry.repeatUnit != null) {
                        Log.d("TAG", "${entry.name} Каждые ${entry.repeatValue} ${entry.repeatUnit}")
                        "${entry.name} Каждые ${entry.repeatValue} ${entry.repeatUnit}"
                    } else {
                        val formattedDate = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(
                            Date(entry.scheduledTime)
                        )
                        Log.d("TAG","${entry.name} в $formattedDate");
                        // Если fixed time
                        "${entry.name} в $formattedDate"
                    }
                    // Можно установить фон
                    setBackgroundResource(R.drawable.event_block_background)
                }
                dayEventContainer.addView(blockView)
            }
        }
    }

}


