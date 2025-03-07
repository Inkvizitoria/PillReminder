package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.model.HistoryScheduleDay
import com.health.pillreminder.data.model.HistoryScheduleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HistorySchedulesFragment : Fragment(R.layout.fragment_history_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistorySchedulesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerHistoryList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistorySchedulesAdapter()
        recyclerView.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Загружаем все графики
            val scheduleEntries: List<ScheduleEntry> =
                AppDatabase.getInstance().scheduleEntryDao().getAllScheduleEntries()

            val scheduleItems = mutableListOf<HistoryScheduleItem>()
            for (schedule in scheduleEntries) {
                // Находим связанное лекарство
                val medicine: Medicine? =
                    AppDatabase.getInstance().medicineDao().getMedicineById(schedule.medicineId)
                if (medicine != null) {
                    // Формируем строку для period
                    val periodStr = if (schedule.repeatValue != null && schedule.repeatUnit != null) {
                        "каждые ${schedule.repeatValue} ${schedule.repeatUnit}"
                    } else {
                        "фиксированное"
                    }
                    // Формируем строку для scheduleTime
                    val scheduleTimeStr = if (schedule.repeatValue != null) {
                        // Предположим, «с periodStart по periodEnd»
                        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        val startStr = sdf.format(Date(schedule.periodStart))
                        val endStr = sdf.format(Date(schedule.periodEnd))
                        "с $startStr по $endStr"
                    } else {
                        // Если фиксированное время
                        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        sdf.format(Date(schedule.scheduledTime))
                    }
                    // Используем periodEnd в качестве даты для группировки
                    scheduleItems.add(
                        HistoryScheduleItem(
                            scheduleId = schedule.id,
                            medicineName = medicine.name,
                            dosage = "${schedule.dosage} ${schedule.dosageUnit}",
                            period = periodStr,
                            scheduleTime = scheduleTimeStr,
                            historyDate = schedule.periodEnd
                        )
                    )
                }
            }
            // Группируем по дню (по historyDate = periodEnd)
            val sdfGroup = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val grouped = scheduleItems.groupBy { sdfGroup.format(Date(it.historyDate)) }
            val days = grouped.map { (dateStr, items) ->
                val cal = Calendar.getInstance()
                cal.time = sdfGroup.parse(dateStr)!!
                HistoryScheduleDay(
                    dateMillis = cal.timeInMillis,
                    items = items
                )
            }.sortedByDescending { it.dateMillis }

            withContext(Dispatchers.Main) {
                if (days.isEmpty()) {
                    Toast.makeText(requireContext(), "История графиков пуста", Toast.LENGTH_SHORT).show()
                }
                adapter.submitList(days)
            }
        }
    }
}
