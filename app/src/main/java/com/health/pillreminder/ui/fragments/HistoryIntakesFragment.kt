package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.HistoryEntry
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.model.HistoryIntakeDay
import com.health.pillreminder.data.model.HistoryIntakeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HistoryIntakesFragment : Fragment(R.layout.fragment_history_by_day) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvCurrentDate: TextView
    private lateinit var btnPrevDate: Button
    private lateinit var btnNextDate: Button
    private lateinit var adapter: HistoryIntakesAdapter

    // По умолчанию – сегодня
    private var currentDate: Calendar = Calendar.getInstance()

    private val sdfHeader = SimpleDateFormat("dd MMMM yyyy, EEEE", Locale.getDefault())
    private val sdfGroup = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate)
        btnPrevDate = view.findViewById(R.id.btnPrevDate)
        btnNextDate = view.findViewById(R.id.btnNextDate)
        recyclerView = view.findViewById(R.id.recyclerHistoryByDay)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Создаем адаптер с callback для удаления записи
        adapter = HistoryIntakesAdapter { historyId ->
            lifecycleScope.launch(Dispatchers.IO) {
                AppDatabase.getInstance().historyDao().deleteById(historyId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
                    loadHistoryForCurrentDate()
                }
            }
        }
        recyclerView.adapter = adapter

        updateHeader()

        btnPrevDate.setOnClickListener {
            currentDate.add(Calendar.DAY_OF_MONTH, -1)
            updateHeader()
            loadHistoryForCurrentDate()
        }
        btnNextDate.setOnClickListener {
            currentDate.add(Calendar.DAY_OF_MONTH, 1)
            updateHeader()
            loadHistoryForCurrentDate()
        }

        loadHistoryForCurrentDate()
    }

    private fun updateHeader() {
        tvCurrentDate.text = sdfHeader.format(currentDate.time)
    }

    fun loadHistoryForCurrentDate() {
        val cal = Calendar.getInstance().apply {
            time = currentDate.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayStart = cal.timeInMillis
        val dayEnd = dayStart + 24 * 60 * 60 * 1000L

        lifecycleScope.launch(Dispatchers.IO) {
            val allHistory: List<HistoryEntry> = AppDatabase.getInstance().historyDao().getAllHistory()
            val filtered = allHistory.filter { it.date in dayStart until dayEnd }

            val intakeItems = mutableListOf<HistoryIntakeItem>()
            for (entry in filtered) {
                val schedule: ScheduleEntry? =
                    AppDatabase.getInstance().scheduleEntryDao().getScheduleEntryById(entry.scheduleEntryId!!)
                if (schedule != null) {
                    val medicine: Medicine? =
                        AppDatabase.getInstance().medicineDao().getMedicineById(schedule.medicineId)
                    if (medicine != null) {
                        val plannedDate = if (schedule.scheduledTime != 0L) schedule.scheduledTime else entry.intakeTime
                        val statusReadable = when (entry.status) {
                            com.health.pillreminder.data.entities.HistoryStatus.TAKEN -> "Принято"
                            com.health.pillreminder.data.entities.HistoryStatus.SKIPPED -> "Пропущено"
                            com.health.pillreminder.data.entities.HistoryStatus.DELETED -> "Удалено"
                        }
                        intakeItems.add(
                            HistoryIntakeItem(
                                historyId = entry.id,
                                medicineName = medicine.name,
                                plannedDate = plannedDate,
                                status = statusReadable,
                                dosage = "${schedule.dosage} ${schedule.dosageUnit}",
                                historyDate = entry.date
                            )
                        )
                    }
                }
            }

            val grouped = intakeItems.groupBy { sdfGroup.format(Date(it.historyDate)) }
            val historyDays = grouped.map { (dateStr, items) ->
                val calGroup = Calendar.getInstance()
                calGroup.time = sdfGroup.parse(dateStr)!!
                HistoryIntakeDay(
                    dateMillis = calGroup.timeInMillis,
                    items = items
                )
            }.sortedByDescending { it.dateMillis }

            withContext(Dispatchers.Main) {
                if (historyDays.isEmpty()) {
                    Toast.makeText(requireContext(), "История за этот день пуста", Toast.LENGTH_SHORT).show()
                }
                adapter.submitList(historyDays)
            }
        }
    }
}
