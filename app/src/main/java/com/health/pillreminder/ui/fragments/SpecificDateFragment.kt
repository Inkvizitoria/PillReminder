package com.health.pillreminder.ui.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.health.pillreminder.R
import com.health.pillreminder.ui.viewmodel.ScheduleSharedViewModel
import java.text.SimpleDateFormat
import java.util.*

class SpecificDateFragment : Fragment(R.layout.fragment_single_tab) {

    private lateinit var viewModel: ScheduleSharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(ScheduleSharedViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnSelectDateTime: Button = view.findViewById(R.id.btnSelectDateTime)
        val tvSelectedDateTime: TextView = view.findViewById(R.id.tvSelectedDateTime)

        btnSelectDateTime.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            datePicker.show(parentFragmentManager, "single_date_picker")
            datePicker.addOnPositiveButtonClickListener { dateSelection ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateSelection

                val timePicker = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    cal.set(Calendar.MINUTE, minute)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val selectedTimestamp = cal.timeInMillis

                    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    tvSelectedDateTime.text = "Выбрано: ${sdf.format(Date(selectedTimestamp))}"

                    // 🔹 Передаем данные в ViewModel
                    viewModel.setSingleTimestamp(selectedTimestamp)
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
                timePicker.show()
            }
        }
    }
}

