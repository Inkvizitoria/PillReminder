package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.model.DayCell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MonthCalendarGridFragment : Fragment(R.layout.fragment_month_calendar_grid_no_offset) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnPrevMonth: Button
    private lateinit var btnNextMonth: Button
    private lateinit var tvMonthYear: TextView

    private var currentYear: Int = 0
    private var currentMonth: Int = 0
    private var scheduleEntries: List<ScheduleEntry> = emptyList()
    private val dayCells = mutableListOf<DayCell>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        recyclerView = view.findViewById(R.id.recyclerMonthGridNoOffset)

        // Вычисляем spanCount адаптивно
        val displayMetrics = resources.displayMetrics
        val cellWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 175f, displayMetrics)
        val screenWidthPx = displayMetrics.widthPixels
        val spanCount = (screenWidthPx / cellWidthPx).toInt().coerceAtLeast(1)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)

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
        lifecycleScope.launch(Dispatchers.IO) {
            scheduleEntries = AppDatabase.getInstance().scheduleEntryDao().getAllScheduleEntries()
            withContext(Dispatchers.Main) {
                createGridCells()
            }
        }
    }

    private fun createGridCells() {
        dayCells.clear()
        val sdf = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
        val tempCal = Calendar.getInstance()
        tempCal.set(Calendar.YEAR, currentYear)
        tempCal.set(Calendar.MONTH, currentMonth)
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        tvMonthYear.text = sdf.format(tempCal.time)

        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfWeekSdf = SimpleDateFormat("EEE", Locale.getDefault())

        for (day in 1..daysInMonth) {
            tempCal.set(Calendar.DAY_OF_MONTH, day)
            tempCal.set(Calendar.HOUR_OF_DAY, 0)
            tempCal.set(Calendar.MINUTE, 0)
            tempCal.set(Calendar.SECOND, 0)
            tempCal.set(Calendar.MILLISECOND, 0)
            val dayStart = tempCal.timeInMillis
            val dayEnd = dayStart + 24 * 60 * 60 * 1000L

            // Фильтруем записи для выбранного дня.
            val entriesForDay = scheduleEntries.filter { entry ->
                if (entry.scheduledTime != 0L) {
                    entry.periodEnd > dayStart && entry.periodStart < dayEnd
                } else if (entry.repeatValue != null && entry.repeatUnit != null) {
                    entry.periodEnd > dayStart && entry.periodStart < dayEnd
                } else false
            }

            dayCells.add(
                DayCell(
                    year = currentYear,
                    month = currentMonth,
                    day = day,
                    dayOfWeek = dayOfWeekSdf.format(tempCal.time),
                    entries = entriesForDay,
                    entriesCount = entriesForDay.size
                )
            )
            Log.d("TAG", "${day}\n${entriesForDay}\n")
        }

        recyclerView.adapter = MonthGridNoOffsetAdapter(dayCells) { cell ->
            val dialog = DayDetailModalFragment.newInstance(cell.year, cell.month, cell.day)
            dialog.show(childFragmentManager, "DayDetailModalFragment")
        }
    }
}
