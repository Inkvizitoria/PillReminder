package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.model.DayBlockData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MonthCalendarFragment : Fragment(R.layout.fragment_month_calendar) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnPrevMonth: Button
    private lateinit var btnNextMonth: Button
    private lateinit var tvMonthYear: TextView

    private var currentYear: Int = 0
    private var currentMonth: Int = 0
    private var scheduleEntries: List<ScheduleEntry> = emptyList()
    private val dayBlocks = mutableListOf<DayBlockData>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        recyclerView = view.findViewById(R.id.recyclerMonthDays)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Инициализируем currentYear и currentMonth
        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH)

        btnPrevMonth.setOnClickListener {
            currentMonth--
            if (currentMonth < 0) {
                currentMonth = 11
                currentYear--
            }
            loadData()
        }

        btnNextMonth.setOnClickListener {
            currentMonth++
            if (currentMonth > 11) {
                currentMonth = 0
                currentYear++
            }
            loadData()
        }

        loadData()
    }

    private fun loadData() {
        // Загружаем все записи из БД, потом фильтруем для текущего месяца
        lifecycleScope.launch(Dispatchers.IO) {
            scheduleEntries = AppDatabase.getInstance().scheduleEntryDao().getAllScheduleEntries()
            withContext(Dispatchers.Main) {
                createDayBlocks()
            }
        }
    }

    private fun createDayBlocks() {
        dayBlocks.clear()

        // Устанавливаем заголовок
        val sdf = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
        val tempCal = Calendar.getInstance()
        tempCal.set(Calendar.YEAR, currentYear)
        tempCal.set(Calendar.MONTH, currentMonth)
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        tvMonthYear.text = sdf.format(tempCal.time)

        // Количество дней в месяце
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..daysInMonth) {
            tempCal.set(Calendar.DAY_OF_MONTH, day)
            tempCal.set(Calendar.HOUR_OF_DAY, 0)
            tempCal.set(Calendar.MINUTE, 0)
            tempCal.set(Calendar.SECOND, 0)
            tempCal.set(Calendar.MILLISECOND, 0)
            val dayStart = tempCal.timeInMillis

            val dayEnd = dayStart + 24*60*60*1000L

            // Фильтруем записи, у которых scheduledTime попадает в этот день
            val entriesForDay = scheduleEntries.filter { it.scheduledTime in dayStart until dayEnd }
            dayBlocks.add(
                DayBlockData(
                    year = currentYear,
                    month = currentMonth,
                    day = day,
                    dayLabel = "$day",
                    dayStart = dayStart,
                    entriesCount = entriesForDay.size
                )
            )
        }

        recyclerView.adapter = DayBlockAdapter(dayBlocks) { dayBlock ->
            // При клике на день открываем DayDetailModalFragment
            val dialog = DayDetailModalFragment.newInstance(dayBlock.year, dayBlock.month, dayBlock.day)
            dialog.show(childFragmentManager, "DayDetailModalFragment")
        }
    }
}


