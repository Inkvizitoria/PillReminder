package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.View
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
import com.health.pillreminder.data.entities.HistoryStatus
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.model.HistoryIntakeDay
import com.health.pillreminder.data.model.HistoryIntakeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HistoryByDayFragment : Fragment(R.layout.fragment_history_by_day) {

    private lateinit var tvCurrentDate: TextView
    private lateinit var btnPrevDate: ImageButton
    private lateinit var btnNextDate: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryByDayAdapter

    // По умолчанию - сегодня
    private var currentDate: Calendar = Calendar.getInstance()

    private val sdfHeader = SimpleDateFormat("dd MMMM yyyy, EEEE", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate)
        btnPrevDate = view.findViewById(R.id.btnPrevDate)
        btnNextDate = view.findViewById(R.id.btnNextDate)
        recyclerView = view.findViewById(R.id.recyclerHistoryByDay)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryByDayAdapter(emptyList())
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

    private fun loadHistoryForCurrentDate() {
        // Определяем начало выбранного дня (00:00) и конец (следующий день)
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
            // Фильтруем историю по полю date для выбранного дня
            val filtered = allHistory.filter { it.date in dayStart until dayEnd }

            // Для каждого HistoryEntry, через связь scheduleEntryId, получаем ScheduleEntry и Medicine,
            // чтобы сформировать HistoryIntakeItem.
            val intakeItems = mutableListOf<HistoryIntakeItem>()
            for (entry in filtered) {
                // Получаем ScheduleEntry
                val schedule: ScheduleEntry? =
                    AppDatabase.getInstance().scheduleEntryDao().getScheduleEntryById(entry.scheduleEntryId!!)
                if (schedule != null) {
                    // Получаем Medicine
                    val medicine: Medicine? =
                        AppDatabase.getInstance().medicineDao().getMedicineById(schedule.medicineId)
                    if (medicine != null) {
                        val plannedDate = if (schedule.scheduledTime != 0L) schedule.scheduledTime else entry.intakeTime
                        // Переводим статус в человекочитаемый вид
                        val statusReadable = when (entry.status) {
                            HistoryStatus.TAKEN -> "Принято"
                            HistoryStatus.SKIPPED -> "Пропущено"
                            HistoryStatus.DELETED -> "Удалено"
                        }
                        intakeItems.add(
                            HistoryIntakeItem(
                                historyId = entry.id,
                                medicineName = medicine.name,
                                intakeTime = entry.intakeTime,
                                plannedDate = plannedDate,
                                status = statusReadable,
                                dosage = "${formatWord(schedule.dosageUnit, schedule.dosage)}",
                                historyDate = entry.date
                            )
                        )
                    }
                }
            }

            // Группируем по дню (по historyDate)
            val sdfGroup = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
                adapter.submitList(historyDays)
            }
        }
    }

    fun formatWord(word: String, count: Number): String {
        // Задаём формы для известных слов
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
        // Форматируем число: если целое – выводим как int, иначе – как double
        val countStr = if (isInteger) countDouble.toInt().toString() else countDouble.toString()

        val form = if (!isInteger) {
            // Для дробных чисел смотрим на целую часть
            val intPart = countDouble.toInt()
            if (intPart in 0..4) form2 else form3
        } else {
            // Для целых чисел стандартное правило склонения
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
