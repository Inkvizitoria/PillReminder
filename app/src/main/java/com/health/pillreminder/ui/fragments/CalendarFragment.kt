package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.ScheduleEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvWeekHeader: TextView
    private var scheduleEntries: List<ScheduleEntry> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        recyclerView = view.findViewById(R.id.recyclerCalendar)
        tvWeekHeader = view.findViewById(R.id.tvWeekHeader)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7) // 7 дней недели
        loadScheduleEntries()
        return view
    }

    private fun loadScheduleEntries() {
        CoroutineScope(Dispatchers.IO).launch {
            val entries = AppDatabase.getInstance().scheduleEntryDao().getAllScheduleEntries()
            withContext(Dispatchers.Main) {
                recyclerView.adapter = CalendarAdapter(entries)
            }
        }
    }


    private fun updateWeekHeader() {
        // Показываем текущую неделю, например, начало и конец недели
        val calendar = Calendar.getInstance()
        // Допустим, неделя начинается с понедельника
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
        val startWeek = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endWeek = sdf.format(calendar.time)
        tvWeekHeader.text = "Неделя: $startWeek - $endWeek"
    }
}
