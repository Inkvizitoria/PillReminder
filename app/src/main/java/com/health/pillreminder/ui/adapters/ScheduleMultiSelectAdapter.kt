package com.health.pillreminder.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.entities.ScheduleEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleMultiSelectAdapter(
    private var schedules: List<ScheduleEntry>
) : RecyclerView.Adapter<ScheduleMultiSelectAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<Long>()

    // Callback для одиночного клика (по умолчанию не используется)
    var onItemClick: ((ScheduleEntry) -> Unit)? = null

    // Callback для выделения элементов (многократный выбор)
    var onItemLongClick: ((ScheduleEntry) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGraphId: TextView = itemView.findViewById(R.id.tvGraphId)
        val tvMedicineName: TextView = itemView.findViewById(R.id.tvMedicineName)
        val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)
        val tvPeriod: TextView = itemView.findViewById(R.id.tvPeriod)
        val tvScheduleTime: TextView = itemView.findViewById(R.id.tvScheduleTime)

        init {
            itemView.setOnClickListener {
                toggleSelection(adapterPosition)
                onItemLongClick?.invoke(schedules[adapterPosition])
            }
        }
    }

    private fun toggleSelection(position: Int) {
        val item = schedules[position]
        if (selectedItems.contains(item.id)) {
            selectedItems.remove(item.id)
        } else {
            selectedItems.add(item.id)
        }
        notifyItemChanged(position)
    }

    fun getSelectedItems(): List<ScheduleEntry> = schedules.filter { selectedItems.contains(it.id) }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun updateData(newSchedules: List<ScheduleEntry>) {
        schedules = newSchedules
        clearSelection()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trash_schedule_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = schedules.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.tvGraphId.text = "График ID: ${schedule.id}"
        holder.tvMedicineName.text = "Лекарство: ${schedule.name}"
        holder.tvDosage.text = "Дозировка: ${schedule.dosage} ${schedule.dosageUnit}"

        val periodStr = if (schedule.repeatValue != null && schedule.repeatUnit != null) {
            "Каждые ${schedule.repeatValue} ${schedule.repeatUnit}"
        } else {
            "-"
        }
        holder.tvPeriod.text = "Период: $periodStr"

        val scheduleTimeStr = if (schedule.repeatValue != null) {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val startStr = sdf.format(Date(schedule.periodStart))
            val endStr = sdf.format(Date(schedule.periodEnd))
            "с $startStr по $endStr"
        } else {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(schedule.scheduledTime))
        }
        holder.tvScheduleTime.text = "Время: ${scheduleTimeStr}"

        // Выделение элементов (синий фон для выбранных)
        if (selectedItems.contains(schedule.id)) {
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#802196F3"))
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
    }

    private fun formatTime(timeMillis: Long): String {
        val hours = timeMillis / 3_600_000
        val minutes = (timeMillis % 3_600_000) / 60_000
        return String.format("%02d:%02d", hours, minutes)
    }
}
