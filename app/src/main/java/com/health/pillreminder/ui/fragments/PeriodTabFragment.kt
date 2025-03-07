package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.health.pillreminder.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PeriodTabFragment : Fragment(R.layout.fragment_period_tab) {

    private lateinit var etRepeatValue: EditText
    private lateinit var spinnerRepeatUnit: Spinner
    private lateinit var btnSelectRange: Button
    private lateinit var tvRangeInfo: TextView

    private var selectedStart: Long = 0L
    private var selectedEnd: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etRepeatValue = view.findViewById(R.id.etRepeatValue)
        spinnerRepeatUnit = view.findViewById(R.id.spinnerRepeatUnit)
        btnSelectRange = view.findViewById(R.id.btnSelectRange)
        tvRangeInfo = view.findViewById(R.id.tvRangeInfo)

        // Настраиваем spinner (например, ["минут", "часов", "дней"])
        val units = listOf("минут", "часов", "дней")
        spinnerRepeatUnit.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)

        // Выбор диапазона дат
        btnSelectRange.setOnClickListener {
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
            val builder = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Выберите период")
                .setCalendarConstraints(constraintsBuilder.build())
            val picker = builder.build()
            picker.show(parentFragmentManager, "date_range_picker")
            picker.addOnPositiveButtonClickListener { selection ->
                if (selection.first != null && selection.second != null) {
                    selectedStart = selection.first!!
                    selectedEnd = selection.second!!
                    val diffDays = ((selectedEnd - selectedStart)/(24*60*60*1000))+1
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val startStr = sdf.format(Date(selectedStart))
                    val endStr = sdf.format(Date(selectedEnd))
                    tvRangeInfo.text = "Период: $startStr - $endStr (дней: $diffDays)"

                    // Сохраняем в родительский фрагмент
                    (parentFragment as? ScheduleCreationFragment)?.periodStart = selectedStart
                    (parentFragment as? ScheduleCreationFragment)?.periodEnd = selectedEnd
                }
            }
        }

        // Сохраняем repeatValue, repeatUnit при каждом изменении
        // (Можно делать при onPause, либо при нажатии "Сохранить" в родителе)
        etRepeatValue.doOnTextChanged { text, _, _, _ ->
            val value = text?.toString()?.toIntOrNull()
            (parentFragment as? ScheduleCreationFragment)?.repeatValue = value
        }
        spinnerRepeatUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val unit = units[position]
                (parentFragment as? ScheduleCreationFragment)?.repeatUnit = unit
            }
        }
    }
}
