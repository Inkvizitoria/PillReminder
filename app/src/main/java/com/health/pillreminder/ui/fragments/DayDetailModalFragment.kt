package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.HistoryEntry
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.model.TimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DayDetailModalFragment : DialogFragment() {

    companion object {
        fun newInstance(year: Int, month: Int, day: Int): DayDetailModalFragment {
            val fragment = DayDetailModalFragment()
            val args = Bundle()
            args.putInt("year", year)
            args.putInt("month", month)
            args.putInt("day", day)
            fragment.arguments = args
            return fragment
        }
    }

    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0

    private lateinit var tvDate: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        year = arguments?.getInt("year") ?: 0
        month = arguments?.getInt("month") ?: 0
        day = arguments?.getInt("day") ?: 0
        //setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppCompat_Dialog)
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View? {
        val view = inflater.inflate(R.layout.fragment_day_detail_modal, container, false)
        tvDate = view.findViewById(R.id.tvDate)
        recyclerView = view.findViewById(R.id.recyclerDayDetail)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }
        tvDate.text = sdf.format(cal.time)

        loadDayEntries()
        return view
    }

    private fun loadDayEntries() {
        val calStart = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayStart = calStart.timeInMillis
        val dayEnd = dayStart + 24 * 60 * 60 * 1000L

        CoroutineScope(Dispatchers.IO).launch {
            val allEntries: List<ScheduleEntry> =
                AppDatabase.getInstance().scheduleEntryDao().getAllScheduleEntries()
            // Фильтрация: если scheduledTime != 0, используем его; иначе, если запись повторяющаяся, проверяем period
            val entriesForDay = allEntries.filter { entry ->
                if (entry.scheduledTime != 0L) {
                    entry.periodEnd > dayStart && entry.periodStart < dayEnd
                } else if (entry.repeatValue != null && entry.repeatUnit != null) {
                    entry.periodEnd > dayStart && entry.periodStart < dayEnd
                } else false
            }

            // Генерируем временные метки (TimeSlot)
            val timeMap = mutableMapOf<Long, MutableList<ScheduleEntry>>()
            for (entry in entriesForDay) {
                if (entry.scheduledTime != 0L) {
                    timeMap.getOrPut(entry.scheduledTime) { mutableListOf() }.add(entry)
                } else if (entry.repeatValue != null && entry.repeatUnit != null) {
                    val unit = entry.repeatUnit.toLowerCase(Locale.getDefault())
                    if (unit in listOf("час", "часов")) {
                        val times = generateRecurringTimesHours(dayStart, entry.repeatValue, entry.activeStartTime, entry.activeEndTime)
                        for (t in times) {
                            timeMap.getOrPut(t) { mutableListOf() }.add(entry)
                        }
                    } else if (unit in listOf("минут", "минуты", "минута")) {
                        val times = generateRecurringTimesMinutes(dayStart, entry.repeatValue, entry.activeStartTime, entry.activeEndTime)
                        for (t in times) {
                            timeMap.getOrPut(t) { mutableListOf() }.add(entry)
                        }
                    }
                }
            }
            val timeSlots = timeMap.map { (timestamp, list) ->
                TimeSlot(timestamp, list)
            }.sortedBy { it.timestamp }

            // Получаем историю для записей данного дня
            val scheduleIds = entriesForDay.map { it.id }
            val historyRecords =
                AppDatabase.getInstance().historyDao().getHistoryForScheduleEntries(scheduleIds)
            // Формируем Map: ключ – Pair(scheduleEntryId, intakeTime), значение – HistoryStatus
            val historyMapByKey = historyRecords.associate { record ->
                Pair(record.scheduleEntryId!!, record.intakeTime) to record.status
            }

            withContext(Dispatchers.Main) {
                recyclerView.adapter = DayDetailAdapterNoEmpty(
                    timeSlots,
                    historyMapByKey
                ) { status, entry, intakeTime ->
                    val historyEntry = HistoryEntry(
                        scheduleEntryId = entry.id,
                        intakeTime = intakeTime,
                        date = System.currentTimeMillis(),
                        status = status,
                        comment = null
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        AppDatabase.getInstance().historyDao().insert(historyEntry)
                        withContext(Dispatchers.Main) {
                            ToastUtils.showCustomToast(
                                requireContext(),
                                "Запись сохранена: $status", ToastType.SUCCESS)
                            loadDayEntries()
                        }
                    }
                }
            }
        }
    }

    private fun generateRecurringTimesHours(
        dayStart: Long,
        repeatValue: Int,
        activeStartTime: Long,
        activeEndTime: Long
    ): List<Long> {
        val result = mutableListOf<Long>()
        val startTime = dayStart + activeStartTime  // активное время начала, например, 10:00
        val endTime = dayStart + activeEndTime        // активное время окончания, например, 19:00
        var time = startTime + repeatValue * 60 * 60 * 1000L
        while (time < endTime) {
            result.add(time)
            time += repeatValue * 60 * 60 * 1000L
        }
        return result
    }

    /**
     * Генерирует времена для повторяющихся событий, заданных в минутах.
     * Начинаем с 00:00, первая метка = 00:00 + repeatValue минут, до 24:00.
     */
    private fun generateRecurringTimesMinutes(
        dayStart: Long,
        repeatValue: Int,
        activeStartTime: Long,
        activeEndTime: Long
    ): List<Long> {
        val result = mutableListOf<Long>()
        val startTime = dayStart + activeStartTime
        val endTime = dayStart + activeEndTime
        var time = startTime + repeatValue * 60 * 1000L
        while (time < endTime) {
            result.add(time)
            time += repeatValue * 60 * 1000L
        }
        return result
    }
}
