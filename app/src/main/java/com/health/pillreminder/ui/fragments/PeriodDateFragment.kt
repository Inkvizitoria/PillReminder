package com.health.pillreminder.ui.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.health.pillreminder.R
import com.health.pillreminder.ui.viewmodel.ScheduleSharedViewModel
import java.text.SimpleDateFormat
import java.util.*

class PeriodDateFragment : Fragment(R.layout.fragment_period_date) {

    private lateinit var viewModel: ScheduleSharedViewModel
    private var periodStart: Long = 0L
    private var periodEnd: Long = 0L
    private var fixedTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(ScheduleSharedViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnSelectRange: Button = view.findViewById(R.id.btnSelectRangePeriod)
        val etFixedTime: EditText = view.findViewById(R.id.etFixedTimePeriod)

        btnSelectRange.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Выберите период")
                .build()
            picker.show(parentFragmentManager, "period_date_picker")
            picker.addOnPositiveButtonClickListener { selection ->
                if (selection.first != null && selection.second != null) {
                    periodStart = selection.first!!
                    periodEnd = selection.second!!

                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    btnSelectRange.text = "Период: ${sdf.format(Date(periodStart))} - ${sdf.format(Date(periodEnd))}"

                    // Передаем в ViewModel с обнулением fixedTime
                    fixedTime = 0L
                    viewModel.setPeriodData(periodStart, periodEnd, fixedTime)
                    Log.d("PeriodDateFragment", "Передача: periodStart=$periodStart, periodEnd=$periodEnd, fixedTime=$fixedTime")
                }
            }
        }

        etFixedTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val timePicker = TimePickerDialog(requireContext(), { _, hour, minute ->
                // Приводим `fixedTime` к UTC
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                fixedTime = cal.timeInMillis % (24 * 60 * 60 * 1000) // Убираем дату, оставляем только время
                etFixedTime.setText(String.format("%02d:%02d", hour, minute))

                // Передаем в ViewModel
                viewModel.setPeriodData(periodStart, periodEnd, fixedTime)
                Log.d("PeriodDateFragment", "Передача фиксированного времени: fixedTime=$fixedTime")
            }, 12, 0, true)
            timePicker.show()
        }
    }
}
