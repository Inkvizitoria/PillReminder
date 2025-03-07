package com.health.pillreminder.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.model.HistoryScheduleDay
import com.health.pillreminder.data.model.HistoryScheduleItem
import java.text.SimpleDateFormat
import java.util.*

private const val VIEW_TYPE_DAY_HEADER = 1
private const val VIEW_TYPE_SCHEDULE_CARD = 2

class HistorySchedulesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dayList = mutableListOf<HistoryScheduleDay>()
    private val items = mutableListOf<Any>() // Содержит HistoryScheduleDay (заголовок) и HistoryScheduleItem (карточки)
    private val sdfDay = SimpleDateFormat("dd.MM.yyyy EEEE", Locale.getDefault())
    private val sdfDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    fun submitList(days: List<HistoryScheduleDay>) {
        dayList.clear()
        dayList.addAll(days)
        rebuildItems()
        notifyDataSetChanged()
    }

    private fun rebuildItems() {
        items.clear()
        for (day in dayList) {
            items.add(day)   // Заголовок дня
            items.addAll(day.items)  // Карточки графиков
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HistoryScheduleDay -> VIEW_TYPE_DAY_HEADER
            is HistoryScheduleItem -> VIEW_TYPE_SCHEDULE_CARD
            else -> throw IllegalStateException("Unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DAY_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_history_day_header, parent, false)
                DayHeaderViewHolder(view)
            }
            VIEW_TYPE_SCHEDULE_CARD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_history_schedule_card, parent, false)
                ScheduleCardViewHolder(view)
            }
            else -> throw IllegalStateException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DayHeaderViewHolder -> holder.bind(items[position] as HistoryScheduleDay)
            is ScheduleCardViewHolder -> holder.bind(items[position] as HistoryScheduleItem)
        }
    }

    inner class DayHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayHeader: TextView = itemView.findViewById(R.id.tvDayHeader)
        private val btnPrevDay: Button = itemView.findViewById(R.id.btnPrevDay)
        private val btnNextDay: Button = itemView.findViewById(R.id.btnNextDay)

        fun bind(day: HistoryScheduleDay) {
            tvDayHeader.text = sdfDate.format(Date(day.dateMillis))
            btnPrevDay.visibility = View.INVISIBLE
            btnNextDay.visibility = View.INVISIBLE
        }
    }

    inner class ScheduleCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGraphId: TextView = itemView.findViewById(R.id.tvGraphId)
        private val tvMedicineName: TextView = itemView.findViewById(R.id.tvMedicineName)
        private val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)
        private val tvPeriod: TextView = itemView.findViewById(R.id.tvPeriod)
        private val tvScheduleTime: TextView = itemView.findViewById(R.id.tvScheduleTime)
        // Кнопка "Удалить" не нужна для истории графиков

        fun bind(item: HistoryScheduleItem) {
            tvGraphId.text = "График ID: ${item.scheduleId}"
            tvMedicineName.text = "Лекарство: ${item.medicineName}"
            tvDosage.text = "Дозировка: ${item.dosage}"
            tvPeriod.text = "Период: ${item.period}"
            tvScheduleTime.text = "Время: ${item.scheduleTime}"
        }
    }
}
