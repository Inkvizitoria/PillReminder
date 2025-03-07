package com.health.pillreminder.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.model.DayBlockData

class DayBlockAdapter(
    private val dayBlocks: List<DayBlockData>,
    private val onDayClick: (DayBlockData) -> Unit
) : RecyclerView.Adapter<DayBlockAdapter.DayBlockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayBlockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day_block, parent, false)
        return DayBlockViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayBlockViewHolder, position: Int) {
        val dayBlock = dayBlocks[position]
        holder.bind(dayBlock)
        holder.itemView.setOnClickListener { onDayClick(dayBlock) }
    }

    override fun getItemCount(): Int = dayBlocks.size

    inner class DayBlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayLabel: TextView = itemView.findViewById(R.id.tvDayLabel)
        private val tvEntriesCount: TextView = itemView.findViewById(R.id.tvEntriesCount)

        fun bind(block: DayBlockData) {
            tvDayLabel.text = block.dayLabel
            if (block.entriesCount > 0) {
                tvEntriesCount.text = "Записей: ${block.entriesCount}"
            } else {
                tvEntriesCount.text = "Нет записей"
            }
        }
    }
}
