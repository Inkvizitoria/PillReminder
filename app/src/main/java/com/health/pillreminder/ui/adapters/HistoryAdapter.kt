package com.health.pillreminder.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.entities.HistoryEntry
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val historyEntries: List<HistoryEntry>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_entry, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val entry = historyEntries[position]
        holder.bind(entry)
    }

    override fun getItemCount(): Int = historyEntries.size

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvHistoryStatus)
        private val tvComment: TextView = itemView.findViewById(R.id.tvHistoryComment)

        fun bind(entry: HistoryEntry) {
            tvDate.text = sdf.format(Date(entry.date))
            tvStatus.text = entry.status.name
            tvComment.text = entry.comment ?: ""
        }
    }
}
