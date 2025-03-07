package com.health.pillreminder.ui.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.health.pillreminder.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SingleTabFragment : Fragment(R.layout.fragment_single_tab) {

    private lateinit var btnSelectDateTime: Button
    private lateinit var tvSelectedDateTime: TextView
    private var selectedTimestamp: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSelectDateTime = view.findViewById(R.id.btnSelectDateTime)
        tvSelectedDateTime = view.findViewById(R.id.tvSelectedDateTime)

        btnSelectDateTime.setOnClickListener {
            // Покажем MaterialDatePicker для выбора даты, а потом TimePicker для времени
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            datePicker.show(parentFragmentManager, "single_date_picker")
            datePicker.addOnPositiveButtonClickListener { dateSelection ->
                // dateSelection – это UTC timestamp начала дня
                // Покажем TimePickerDialog
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateSelection
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH)
                val day = cal.get(Calendar.DAY_OF_MONTH)

                // Запускаем TimePickerDialog
                val timePicker = TimePickerDialog(requireContext(),
                    { _, hourOfDay, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        cal.set(Calendar.MINUTE, minute)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        selectedTimestamp = cal.timeInMillis
                        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        tvSelectedDateTime.text = "Выбрано: ${sdf.format(Date(selectedTimestamp))}"

                        // Сохраняем в родительский фрагмент
                        (parentFragment as? ScheduleCreationFragment)?.singleTimestamp = selectedTimestamp
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            }
        }
    }
}
