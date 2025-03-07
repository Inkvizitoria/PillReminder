package com.health.pillreminder.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.ScheduleEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DayDetailDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_DAY_START = "arg_day_start"
        fun newInstance(dayStart: Long): DayDetailDialogFragment {
            val fragment = DayDetailDialogFragment()
            val args = Bundle()
            args.putLong(ARG_DAY_START, dayStart)
            fragment.arguments = args
            return fragment
        }
    }

    private var dayStart: Long = 0L
    private var scheduleEntries: List<ScheduleEntry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dayStart = arguments?.getLong(ARG_DAY_START) ?: 0L
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(createContentView())
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return dialog
    }

    private fun createContentView(): View {
        val scrollView = ScrollView(requireContext())
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        scrollView.addView(container)

        // Заголовок с датой
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val header = TextView(requireContext()).apply {
            text = sdf.format(Date(dayStart))
            textSize = 20f
        }
        container.addView(header)

        // Загружаем записи для данного дня
        CoroutineScope(Dispatchers.IO).launch {
            val dayEnd = dayStart + 24 * 60 * 60 * 1000L
            scheduleEntries = AppDatabase.getInstance().scheduleEntryDao().getAllScheduleEntries()
                .filter { it.scheduledTime in dayStart until dayEnd }
            withContext(Dispatchers.Main) {
                if (scheduleEntries.isEmpty()) {
                    container.addView(TextView(requireContext()).apply {
                        text = "Нет записей для этого дня."
                    })
                } else {
                    // Для каждого часа в дне (например, с 6:00 до 23:00) выводим записи
                    for (hour in 6..23) {
                        val hourStart = getTimeForHour(dayStart, hour)
                        val hourEnd = getTimeForHour(dayStart, hour + 1)
                        val entriesForHour = scheduleEntries.filter { it.scheduledTime in hourStart until hourEnd }
                        if (entriesForHour.isNotEmpty()) {
                            val hourLabel = TextView(requireContext()).apply {
                                text = String.format("%02d:00", hour)
                                textSize = 16f
                                setPadding(0, 16, 0, 4)
                            }
                            container.addView(hourLabel)
                            for (entry in entriesForHour) {
                                val entryView = TextView(requireContext()).apply {
                                    text = "• ${entry.dosage}${entry.dosageUnit}"
                                    setPadding(16, 4, 0, 4)
                                }
                                container.addView(entryView)
                            }
                        }
                    }
                }
            }
        }

        return scrollView
    }

    private fun getTimeForHour(dayStart: Long, hour: Int): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dayStart
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
