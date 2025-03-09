package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.util.Log
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
import com.health.pillreminder.ui.dialogs.EditScheduleDialogFragment
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

        adapter = HistorySchedulesAdapter(
            onEditSchedule = { schedule -> editSchedule(schedule) },
            onDeleteSchedule = { schedule -> deleteSchedule(schedule) }
        )

        recyclerView.adapter = adapter
        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            val scheduleEntries: List<ScheduleEntry> =
                AppDatabase.getInstance().scheduleEntryDao().getAllScheduleEntries()

            val scheduleItems = mutableListOf<HistoryScheduleItem>()
            for (schedule in scheduleEntries) {
                if (!schedule.isDeleted) {  // Исключаем удаленные графики
                    val medicine: Medicine? =
                        AppDatabase.getInstance().medicineDao().getMedicineById(schedule.medicineId)
                    if (medicine != null) {
                        val periodStr =
                            if (schedule.repeatValue != null && schedule.repeatUnit != null) {
                                "Каждые ${formatWord(schedule.repeatUnit, schedule.repeatValue)}"
                            } else {
                                "-"
                            }
                        val scheduleTimeStr = if (schedule.repeatValue != null) {
                            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            val startStr = sdf.format(Date(schedule.periodStart))
                            val endStr = sdf.format(Date(schedule.periodEnd))
                            "с $startStr по $endStr"
                        } else {
                            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            sdf.format(Date(schedule.scheduledTime))
                        }
                        scheduleItems.add(
                            HistoryScheduleItem(
                                scheduleId = schedule.id,
                                medicineName = medicine.name,
                                dosage = "${formatWord(schedule.dosageUnit, schedule.dosage)}",
                                period = periodStr,
                                scheduleTime = scheduleTimeStr,
                                historyDate = schedule.periodEnd,
                                activeStartTime = schedule.activeStartTime,
                                activeEndTime = schedule.activeEndTime,
                            )
                        )
                    }
                }
            }
            val sdfGroup = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val grouped = scheduleItems.groupBy { sdfGroup.format(Date(it.historyDate)) }
            val days = grouped.map { (dateStr, items) ->
                val cal = Calendar.getInstance()
                cal.time = sdfGroup.parse(dateStr)!!
                HistoryScheduleDay(
                    dateMillis = cal.timeInMillis,
                    items = items
                )
            }

            withContext(Dispatchers.Main) {
                adapter.submitList(days)
            }
        }
    }

    private fun deleteSchedule(schedule: HistoryScheduleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance().scheduleEntryDao()
                db.markScheduleAsDeleted(schedule.scheduleId)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "График перемещен в корзину", Toast.LENGTH_SHORT).show()
                    loadHistory()
                }
            } catch (e: Exception) {
                Log.e("HistorySchedulesFragment", "Ошибка удаления графика", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun editSchedule(schedule: HistoryScheduleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance().scheduleEntryDao()
            val scheduleEntry = db.getScheduleEntryById(schedule.scheduleId)

            withContext(Dispatchers.Main) {
                scheduleEntry?.let { entry ->
                    EditScheduleDialogFragment(entry) {
                        loadHistory() // Перезагружаем историю после редактирования
                    }.show(parentFragmentManager, "edit_schedule")
                } ?: Toast.makeText(requireContext(), "Ошибка загрузки графика", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun formatWord(word: String, count: Number): String {
        val forms = when (word.lowercase(Locale.ROOT)) {
            "час", "часы", "часов" -> Triple("час", "часа", "часов")
            "день", "дни", "дней" -> Triple("день", "дня", "дней")
            "минута", "минуты", "минут" -> Triple("минута", "минуты", "минут")
            "таблетка", "таблетки" -> Triple("таблетка", "таблетки", "таблеток")
            "миллиграмм", "миллиграммы" -> Triple("миллиграмм", "миллиграмма", "миллиграмм")
            else -> return "$count $word"
        }
        val (form1, form2, form3) = forms

        val countDouble = count.toDouble()
        val isInteger = countDouble % 1.0 == 0.0
        val countStr = if (isInteger) countDouble.toInt().toString() else countDouble.toString()

        val form = if (!isInteger) {
            val intPart = countDouble.toInt()
            if (intPart in 0..4) form2 else form3
        } else {
            val intVal = countDouble.toInt()
            val n = intVal % 100
            if (n in 11..19) {
                form3
            } else {
                when (intVal % 10) {
                    1 -> form1
                    2, 3, 4 -> form2
                    else -> form3
                }
            }
        }
        return "$countStr $form"
    }
}
