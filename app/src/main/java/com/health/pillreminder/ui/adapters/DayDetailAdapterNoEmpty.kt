package com.health.pillreminder.ui.fragments

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

                    tvEventText.text =
                        "Принять ${entry.name} в дозировке ${entry.dosage} ${entry.dosageUnit}"
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
}
