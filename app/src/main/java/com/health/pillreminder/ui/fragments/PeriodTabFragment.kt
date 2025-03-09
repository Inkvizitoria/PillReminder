package com.health.pillreminder.ui.fragments

import android.app.TimePickerDialog
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
import com.google.android.material.textfield.TextInputEditText
import com.health.pillreminder.R
import java.text.SimpleDateFormat
import java.util.*

class PeriodTabFragment : Fragment(R.layout.fragment_period_tab) {

    private lateinit var etRepeatValue: EditText
    private lateinit var spinnerRepeatUnit: Spinner
    private lateinit var btnSelectRange: Button
    private lateinit var tvRangeInfo: TextView

    // Новые элементы для выбора активного периода
    private lateinit var etActiveStart: TextInputEditText
    private lateinit var etActiveEnd: TextInputEditText

    private var selectedStart: Long = 0L
    private var selectedEnd: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etRepeatValue = view.findViewById(R.id.etRepeatValue)
        spinnerRepeatUnit = view.findViewById(R.id.spinnerRepeatUnit)
        btnSelectRange = view.findViewById(R.id.btnSelectRange)
        tvRangeInfo = view.findViewById(R.id.tvRangeInfo)
        etActiveStart = view.findViewById(R.id.etActiveStart)
        etActiveEnd = view.findViewById(R.id.etActiveEnd)

        // Настраиваем Spinner для единиц повторения
        val units = listOf("Минут", "Часов")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_dropdown_item, units)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerRepeatUnit.adapter = adapter

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
                    val diffDays = ((selectedEnd - selectedStart) / (24 * 60 * 60 * 1000)) + 1
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val startStr = sdf.format(Date(selectedStart))
                    val endStr = sdf.format(Date(selectedEnd))
                    tvRangeInfo.text = "Период: $startStr - $endStr (дней: $diffDays)"

                    // Сохраняем период в родительский фрагмент (ScheduleCreationFragment)
                    (parentFragment as? ScheduleCreationFragment)?.apply {
                        periodStart = selectedStart
                        periodEnd = selectedEnd
                    }
                }
            }
        }

        // Обработка repeatValue и repeatUnit
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

        // Настройка TimePicker для выбора активного периода
        // Дефолт: "От" = 00:00, "До" = 23:59
        etActiveStart.setOnClickListener {
            // По умолчанию activeStartTime = 0 (00:00)
            val parentSC = parentFragment as? ScheduleCreationFragment
            val defaultStart = 0L
            val currentStart = parentSC?.activeStartTime ?: defaultStart
            val cal = Calendar.getInstance().apply { timeInMillis = currentStart }
            val timePicker = TimePickerDialog(requireContext(),
                { _, hourOfDay, minute ->
                    val timeMs = hourOfDay * 60 * 60 * 1000L + minute * 60 * 1000L
                    etActiveStart.setText(String.format("%02d:%02d", hourOfDay, minute))
                    parentSC?.activeStartTime = timeMs
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }
        etActiveEnd.setOnClickListener {
            // Дефолт: activeEndTime = 23:59 (23*3600000 + 59*60000 = 86340000 мс)
            val parentSC = parentFragment as? ScheduleCreationFragment
            val defaultEnd = 23 * 60 * 60 * 1000L + 59 * 60 * 1000L
            val currentEnd = parentSC?.activeEndTime ?: defaultEnd
            val cal = Calendar.getInstance().apply { timeInMillis = currentEnd }
            val timePicker = TimePickerDialog(requireContext(),
                { _, hourOfDay, minute ->
                    val timeMs = hourOfDay * 60 * 60 * 1000L + minute * 60 * 1000L
                    etActiveEnd.setText(String.format("%02d:%02d", hourOfDay, minute))
                    parentSC?.activeEndTime = timeMs
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }
    }
}
