package com.health.pillreminder.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.entities.ScheduleEntry
import java.util.*

class DayHourAdapter(
    private val entries: List<ScheduleEntry>,
    private val dayStart: Long  // начало выбранного дня (00:00)
) : RecyclerView.Adapter<DayHourAdapter.HourViewHolder>() {

    override fun getItemCount(): Int = 24

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hour_block, parent, false)
        return HourViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {
        // Рассчитываем диапазон для часа: [hourStart, hourEnd)
        val hourStart = dayStart + position * 60 * 60 * 1000L
        val hourEnd = hourStart + 60 * 60 * 1000L
        val events = mutableListOf<ScheduleEntry>()
        for (entry in entries) {
            // Если фиксированная запись и scheduledTime попадает в данный час
            if (entry.scheduledTime in hourStart until hourEnd) {
                events.add(entry)
            }
            // Если запись повторяющаяся
            else if (entry.repeatValue != null && entry.repeatUnit != null) {
                val unit = entry.repeatUnit.toLowerCase(Locale.getDefault())
                if (unit in listOf("час", "часов")) {
                    val recurringTimes = generateRecurringTimesForHours(dayStart, entry.repeatValue)
                    if (recurringTimes.any { it in hourStart until hourEnd }) {
                        events.add(entry)
                    }
                } else if (unit in listOf("минут", "минуты", "минута")) {
                    val recurringTimes = generateRecurringTimesForMinutes(dayStart, entry.repeatValue)
                    if (recurringTimes.any { it in hourStart until hourEnd }) {
                        events.add(entry)
                    }
                }
            }
        }
        holder.bind(position, events)
    }

    /**
     * Генерирует времена для повторяющихся событий, заданных в часах.
     * Начинаем с 4:00, первая метка = 4:00 + repeatValue часов, до 24:00.
     */
    private fun generateRecurringTimesForHours(dayStart: Long, repeatValue: Int): List<Long> {
        val result = mutableListOf<Long>()
        val startTime = dayStart + 4 * 60 * 60 * 1000L  // 4:00 AM
        var time = startTime + repeatValue * 60 * 60 * 1000L
        val endTime = dayStart + 24 * 60 * 60 * 1000L
        while (time < endTime) {
            result.add(time)
            time += repeatValue * 60 * 60 * 1000L
        }
        return result
    }

    /**
     * Генерирует времена для повторяющихся событий, заданных в минутах.
     * Начинаем с 4:00, первая метка = 4:00 + repeatValue минут, до 24:00.
     */
    private fun generateRecurringTimesForMinutes(dayStart: Long, repeatValue: Int): List<Long> {
        val result = mutableListOf<Long>()
        val startTime = dayStart + 4 * 60 * 60 * 1000L  // 4:00 AM
        var time = startTime + repeatValue * 60 * 1000L
        val endTime = dayStart + 24 * 60 * 60 * 1000L
        while (time < endTime) {
            result.add(time)
            time += repeatValue * 60 * 1000L
        }
        return result
    }

    inner class HourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHour: TextView = itemView.findViewById(R.id.tvHour)
        private val tvEntriesHour: TextView = itemView.findViewById(R.id.tvEntriesHour)

        fun bind(hour: Int, hourEntries: List<ScheduleEntry>) {
            tvHour.text = String.format("%02d:00", hour)
            tvEntriesHour.text = if (hourEntries.isEmpty()) {
                ""
            } else {
                hourEntries.joinToString(separator = "\n") { entry ->
                    "Принять ${entry.name} в дозировке ${entry.dosage} ${entry.dosageUnit}"
                }
            }
        }
    }
}
