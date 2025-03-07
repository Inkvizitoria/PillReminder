package com.health.pillreminder.ui.fragments

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.entities.ScheduleEntry
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(private val scheduleEntries: List<ScheduleEntry>) :
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    // Вычисляем начало недели: устанавливаем понедельник текущей недели, время сбрасываем до 00:00
    private val weekStart: Long = run {
        val calendar = Calendar.getInstance()
        // Устанавливаем понедельник как первый день недели
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val tvEntries: TextView = itemView.findViewById(R.id.tvEntries)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun getItemCount(): Int = 7  // Неделя из 7 дней

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        // Для каждого дня недели вычисляем диапазон от 00:00 до 00:00 следующего дня
        val dayStart = weekStart + position * 24 * 60 * 60 * 1000L
        val dayEnd = dayStart + 24 * 60 * 60 * 1000L

        // Форматируем дату для отображения
        val sdf = SimpleDateFormat("EEE, dd", Locale.getDefault())
        val dayDate = Date(dayStart)
        holder.tvDay.text = sdf.format(dayDate)

        // Отладочное логирование:
        Log.d("CalendarAdapter", "Day $position: dayStart=$dayStart, dayEnd=$dayEnd")

        // Фильтруем записи, попадающие в этот день
        val entriesForDay = scheduleEntries.filter {
            // Добавляем проверку, что scheduledTime находится между dayStart и dayEnd
            it.scheduledTime in dayStart until dayEnd
        }

        // Выводим отладочную информацию для каждой записи (при необходимости)
        entriesForDay.forEach {
            Log.d("CalendarAdapter", "Entry scheduledTime: ${it.scheduledTime}")
        }

        holder.tvEntries.text = if (entriesForDay.isNotEmpty()) {
            // Можно вывести подробности или количество
            "Записей: ${entriesForDay.size}"
        } else {
            "Нет записей"
        }
    }
}
