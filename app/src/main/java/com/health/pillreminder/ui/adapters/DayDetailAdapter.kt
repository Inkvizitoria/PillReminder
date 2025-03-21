package com.health.pillreminder.ui.fragments

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.entities.ScheduleEntry
import java.text.SimpleDateFormat
import java.util.*

class DayDetailAdapter(
    private val entries: List<ScheduleEntry>,
    private val dayStart: Long // начало дня (00:00 местное время)
) : RecyclerView.Adapter<DayDetailAdapter.TimeSlotViewHolder>() {

    private val slotDuration = 5 * 60 * 1000L  // 5 минут в миллисекундах
    private val totalSlots = (24 * 60) / 5       // 288 слотов за день

    override fun getItemCount(): Int = totalSlots

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val slotStart = dayStart + position * slotDuration
        val slotEnd = slotStart + slotDuration

        // Каждые 12 слотов (то есть на целый час) показываем метку времени
        val showLabel = position % 12 == 0
        val timeLabel = if (showLabel) {
            val cal = Calendar.getInstance().apply { timeInMillis = slotStart }
            String.format("%02d:00", cal.get(Calendar.HOUR_OF_DAY))
        } else ""

        val slotEvents = mutableListOf<ScheduleEntry>()
        for (entry in entries) {
            // Если фиксированная запись и scheduledTime попадает в данный слот
            if (entry.scheduledTime != 0L) {
                if (entry.scheduledTime in slotStart until slotEnd) {
                    slotEvents.add(entry)
                }
            }
            // Если запись повторяющаяся по минутам
            else if (entry.repeatValue != null && entry.repeatUnit != null &&
                entry.repeatUnit.toLowerCase(Locale.getDefault()) in listOf("минут", "минуты", "минута")
            ) {
                val recurringTimes = if (entry.activeStartTime > 0 && entry.activeEndTime > 0)
                    generateRecurringTimesForMinutes(dayStart, entry.repeatValue, entry.activeStartTime, entry.activeEndTime)
                else
                    generateRecurringTimesForMinutes(dayStart, entry.repeatValue)
                if (recurringTimes.any { it in slotStart until slotEnd }) {
                    slotEvents.add(entry)
                }
            }
            // Если запись повторяющаяся по часам
            else if (entry.repeatValue != null && entry.repeatUnit != null &&
                entry.repeatUnit.toLowerCase(Locale.getDefault()) in listOf("час", "часов")
            ) {
                val recurringTimes = if (entry.activeStartTime > 0 && entry.activeEndTime > 0)
                    generateRecurringTimesForHours(dayStart, entry.repeatValue, entry.activeStartTime, entry.activeEndTime)
                else
                    generateRecurringTimesForHours(dayStart, entry.repeatValue)
                if (recurringTimes.any { it in slotStart until slotEnd }) {
                    slotEvents.add(entry)
                }
            }
        }
        holder.bind(timeLabel, slotEvents)
    }

    /**
     * Генерирует времена для повторяющихся событий, заданных в минутах, для всего дня.
     * Начинаем с 00:00, первая метка = 00:00 + repeatValue минут, до 24:00.
     */
    private fun generateRecurringTimesForMinutes(dayStart: Long, repeatValue: Int): List<Long> {
        val result = mutableListOf<Long>()
        var time = dayStart
        val endTime = dayStart + 24 * 60 * 60 * 1000L
        while (time < endTime) {
            result.add(time)
            time += repeatValue * 60 * 1000L
        }
        return result
    }

    /**
     * Генерирует времена для повторяющихся событий, заданных в часах, для всего дня.
     * Начинаем с 00:00, первая метка = 00:00 + repeatValue часов, до 24:00.
     */
    private fun generateRecurringTimesForHours(dayStart: Long, repeatValue: Int): List<Long> {
        val result = mutableListOf<Long>()
        var time = dayStart
        val endTime = dayStart + 24 * 60 * 60 * 1000L
        while (time < endTime) {
            result.add(time)
            time += repeatValue * 60 * 60 * 1000L
        }
        return result
    }

    /**
     * Генерирует времена для повторяющихся событий, заданных в минутах,
     * с учетом активного периода графика.
     * Генерация происходит с (dayStart + activeStartTime) до (dayStart + activeEndTime).
     */
    private fun generateRecurringTimesForMinutes(
        dayStart: Long,
        repeatValue: Int,
        activeStartTime: Long,
        activeEndTime: Long
    ): List<Long> {
        val result = mutableListOf<Long>()
        val startTime = dayStart + activeStartTime
        val endTime = dayStart + activeEndTime
        var time = startTime + repeatValue * 60 * 1000L
        while (time < endTime) {
            result.add(time)
            time += repeatValue * 60 * 1000L
        }
        return result
    }

    /**
     * Генерирует времена для повторяющихся событий, заданных в часах,
     * с учетом активного периода графика.
     * Генерация происходит с (dayStart + activeStartTime) до (dayStart + activeEndTime).
     */
    private fun generateRecurringTimesForHours(
        dayStart: Long,
        repeatValue: Int,
        activeStartTime: Long,
        activeEndTime: Long
    ): List<Long> {
        val result = mutableListOf<Long>()
        val startTime = dayStart + activeStartTime
        val endTime = dayStart + activeEndTime
        var time = startTime + repeatValue * 60 * 60 * 1000L
        while (time < endTime) {
            result.add(time)
            time += repeatValue * 60 * 60 * 1000L
        }
        return result
    }

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTimeLabel: TextView = itemView.findViewById(R.id.tvTimeLabel)
        private val tvEvents: TextView = itemView.findViewById(R.id.tvEvents)

        fun bind(timeLabel: String, slotEvents: List<ScheduleEntry>) {
            tvTimeLabel.text = timeLabel
            tvEvents.text = if (slotEvents.isEmpty()) {
                ""
            } else {
                slotEvents.joinToString(separator = "\n") { entry ->
                    "Принять ${entry.name} в дозировке ${entry.dosage} ${entry.dosageUnit}"
                }
            }
        }
    }
}
