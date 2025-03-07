/*
package com.health.pillreminder.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.model.HistoryIntakeDay
import com.health.pillreminder.data.model.HistoryIntakeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val VIEW_TYPE_DAY_HEADER = 1
private const val VIEW_TYPE_INTAKE_CARD = 2

class HistoryIntakesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dayList = mutableListOf<HistoryIntakeDay>()
    private val items = mutableListOf<Any>()  // Содержит HistoryIntakeDay (заголовок) и HistoryIntakeItem (карточки)
    private val sdfDay = SimpleDateFormat("dd.MM.yyyy EEEE", Locale.getDefault())
    private val sdfDateTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun submitList(days: List<HistoryIntakeDay>) {
        dayList.clear()
        dayList.addAll(days)
        rebuildItems()
        notifyDataSetChanged()
    }

    private fun rebuildItems() {
        items.clear()
        for (day in dayList) {
            items.add(day)  // Заголовок дня
            items.addAll(day.items)  // Карточки приёмов
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HistoryIntakeDay -> VIEW_TYPE_DAY_HEADER
            is HistoryIntakeItem -> VIEW_TYPE_INTAKE_CARD
            else -> throw IllegalStateException("Unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            VIEW_TYPE_DAY_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_day_header, parent, false)
                DayHeaderViewHolder(view)
            }
            VIEW_TYPE_INTAKE_CARD -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_intake_card, parent, false)
                IntakeCardViewHolder(view)
            }
            else -> throw IllegalStateException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is DayHeaderViewHolder -> holder.bind(items[position] as HistoryIntakeDay)
            is IntakeCardViewHolder -> holder.bind(items[position] as HistoryIntakeItem)
        }
    }

    inner class DayHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayHeader: TextView = itemView.findViewById(R.id.tvDayHeader)
        private val btnPrevDay: Button = itemView.findViewById(R.id.btnPrevDay)
        private val btnNextDay: Button = itemView.findViewById(R.id.btnNextDay)

        fun bind(day: HistoryIntakeDay) {
            tvDayHeader.text = sdfDay.format(Date(day.dateMillis))
            btnPrevDay.visibility = View.INVISIBLE
            btnNextDay.visibility = View.INVISIBLE
        }
    }

    inner class IntakeCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMedicine: TextView = itemView.findViewById(R.id.tvMedicine)
        private val tvPlannedDate: TextView = itemView.findViewById(R.id.tvPlannedDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(item: HistoryIntakeItem) {
            tvMedicine.text = "Лекарство: ${item.medicineName}"
            tvPlannedDate.text = "Запланированная дата приёма: ${sdfDateTime.format(Date(item.plannedDate))}"
            tvStatus.text = "Статус: ${item.status}"
            tvDosage.text = "Дозировка: ${item.dosage}"
            btnDelete.setOnClickListener {
                // Удаляем запись из БД истории и обновляем список
                // Для этого используем корутину, которая вызывает deleteById
                (itemView.context as? AppCompatActivity)?.let { activity ->
                    activity.lifecycleScope.launch(Dispatchers.IO) {
                        AppDatabase.getInstance().historyDao().deleteById(item.historyId)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "Запись удалена", Toast.LENGTH_SHORT).show()
                            // Перезагружаем историю (если фрагмент реализован с такой возможностью)
                            if (itemView.context is HistoryIntakesFragment) {
                                (itemView.context as HistoryIntakesFragment).loadHistoryForCurrentDate()
                            }
                        }
                    }
                }
            }
        }
    }
}
*/
