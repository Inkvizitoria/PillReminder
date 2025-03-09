package com.health.pillreminder.ui.fragments

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.entities.HistoryStatus
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.model.TimeSlot
import java.text.SimpleDateFormat
import java.util.*



class DayDetailAdapterNoEmpty(
    private val timeSlots: List<TimeSlot>,
    // historyMap: ключ – Pair(scheduleEntryId, intakeTime), значение – сохраненный статус
    private val historyMap: Map<Pair<Long, Long>, HistoryStatus>,
    // Callback для сохранения конкретного приёма: (status, ScheduleEntry, intakeTime) -> Unit
    private val onHistorySaved: (HistoryStatus, ScheduleEntry, Long) -> Unit
) : RecyclerView.Adapter<DayDetailAdapterNoEmpty.TimeSlotViewHolder>() {

    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getItemCount(): Int = timeSlots.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot_no_empty, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(timeSlots[position])
    }

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTimeLabel: TextView = itemView.findViewById(R.id.tvTimeLabel)
        private val eventContainer: LinearLayout = itemView.findViewById(R.id.eventContainer)

        fun bind(slot: TimeSlot) {
            tvTimeLabel.text = sdf.format(Date(slot.timestamp))
            eventContainer.removeAllViews()
            for (entry in slot.entries) {
                if (!entry.isDeleted) {
                    val blockView = LayoutInflater.from(eventContainer.context)
                        .inflate(R.layout.item_event_block, eventContainer, false)
                    val tvEventText = blockView.findViewById<TextView>(R.id.tvEventText)
                    val btnAccept = blockView.findViewById<Button>(R.id.btnAccept)
                    val btnNotAccept = blockView.findViewById<Button>(R.id.btnNotAccept)
                    val tvEventTextContainer: LinearLayout = blockView.findViewById(R.id.tvEventTextContainer)
                    try {
                        val context = tvEventTextContainer.context
                        // Формируем имя drawable по ключу цвета из БД.
                        val drawableName = "item_${entry.blockColor}"
                        val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                        if (resId != 0) {
                            tvEventTextContainer.setBackgroundResource(resId)
                        } else {
                            tvEventTextContainer.setBackgroundColor(Color.LTGRAY)
                        }
                    } catch (e: Exception) {
                        tvEventTextContainer.setBackgroundColor(Color.LTGRAY)
                    }
                    tvEventText.text =
                        "Принять ${entry.name} в дозировке ${entry.dosage} ${entry.dosageUnit}"
                    tvEventText.setTextColor(getContrastingTextColor(colorMapping[entry.blockColor] ?: "#FFFFFF"))

                    // Ключ для истории: (scheduleEntryId, slot.timestamp)
                    val key = Pair(entry.id, slot.timestamp)
                    val savedStatus = historyMap[key]
                    if (savedStatus != null) {
                        when (savedStatus) {
                            HistoryStatus.TAKEN -> blockView.setBackgroundResource(R.drawable.event_block_background_taken)
                            HistoryStatus.SKIPPED -> blockView.setBackgroundResource(R.drawable.event_block_background_skipped)
                            else -> {}
                        }
                        btnAccept.isEnabled = false
                        btnNotAccept.isEnabled = false
                    } else {
                        btnAccept.setOnClickListener {
                            onHistorySaved(HistoryStatus.TAKEN, entry, slot.timestamp)
                        }
                        btnNotAccept.setOnClickListener {
                            onHistorySaved(HistoryStatus.SKIPPED, entry, slot.timestamp)
                        }
                    }
                    eventContainer.addView(blockView)
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
