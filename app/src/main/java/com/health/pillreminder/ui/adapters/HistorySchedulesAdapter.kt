package com.health.pillreminder.ui.fragments

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.model.HistoryScheduleDay
import com.health.pillreminder.data.model.HistoryScheduleItem
import com.health.pillreminder.ui.dialogs.EditScheduleDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val VIEW_TYPE_DAY_HEADER = 1
private const val VIEW_TYPE_SCHEDULE_CARD = 2

class HistorySchedulesAdapter(
    private val onEditSchedule: (HistoryScheduleItem) -> Unit,
    private val onDeleteSchedule: (HistoryScheduleItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dayList = mutableListOf<HistoryScheduleDay>()
    private val items = mutableListOf<Any>()

    fun submitList(days: List<HistoryScheduleDay>) {
        dayList.clear()
        dayList.addAll(days)
        rebuildItems()
        notifyDataSetChanged()
    }

    private fun rebuildItems() {
        items.clear()
        for (day in dayList) {
            items.add(day)
            items.addAll(day.items)
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
                    .inflate(R.layout.item_history_sheduler_day_header, parent, false)
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
        fun bind(day: HistoryScheduleDay) {
            // Заголовок дня (если нужно, можно добавить текст)
        }
    }

    inner class ScheduleCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGraphId: TextView = itemView.findViewById(R.id.tvGraphId)
        private val tvMedicineName: TextView = itemView.findViewById(R.id.tvMedicineName)
        private val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)
        private val tvPeriod: TextView = itemView.findViewById(R.id.tvPeriod)
        private val tvScheduleTime: TextView = itemView.findViewById(R.id.tvScheduleTime)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEditSchedule)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteSchedule)


        fun bind(item: HistoryScheduleItem) {
            tvGraphId.text = "График ID: ${item.scheduleId}"
            tvMedicineName.text = "Лекарство: ${item.medicineName}"
            tvDosage.text = "Дозировка: ${item.dosage}"

            btnEdit.setOnClickListener {
                val context = itemView.context
                val scheduleDao = AppDatabase.getInstance().scheduleEntryDao()

                // Загружаем ScheduleEntry из базы перед открытием диалога
                CoroutineScope(Dispatchers.IO).launch {
                    val scheduleEntry = scheduleDao.getScheduleEntryById(item.scheduleId)

                    withContext(Dispatchers.Main) {
                        scheduleEntry?.let { entry ->
                            EditScheduleDialogFragment(entry) {
                                notifyDataSetChanged() // Обновляем список после редактирования
                            }.show((context as AppCompatActivity).supportFragmentManager, "edit_schedule")
                        } ?: ToastUtils.showCustomToast(context, "Ошибка загрузки графика", ToastType.ERROR)
                    }
                }
            }



            if (item.period.isNotEmpty()) {
                Log.d("TEST", item.activeStartTime.toString())
                if (item.activeStartTime != 0L) {
                    tvPeriod.text = "Период: ${item.period} начиная с ${formatTime(item.activeStartTime)} до ${formatTime(item.activeEndTime)}"
                } else {
                    tvPeriod.text = "Период: ${item.period}"
                }
            } else {
                tvPeriod.text = "-"
            }

            tvScheduleTime.text = "Время: ${item.scheduleTime}"

            btnEdit.setOnClickListener { onEditSchedule(item) }
            btnDelete.setOnClickListener { onDeleteSchedule(item) }
        }
    }

    fun formatTime(timeMillis: Long): String {
        val hours = timeMillis / 3_600_000
        val minutes = (timeMillis % 3_600_000) / 60_000
        return String.format("%02d:%02d", hours, minutes)
    }
}
