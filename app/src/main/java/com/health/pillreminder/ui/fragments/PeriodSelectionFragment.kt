package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.health.pillreminder.R
import kotlin.properties.Delegates

class PeriodSelectionFragment : Fragment() {

    interface PeriodSelectionListener {
        fun onPeriodSelected(periodDays: Int, start: Long, end: Long)
    }

    var periodSelectionListener: PeriodSelectionListener? = null

    private lateinit var btnSelectRange: Button
    private lateinit var tvSelectedRange: TextView
    private lateinit var btnNext: Button

    private var selectedPeriodDays: Int = 1
    private var start by Delegates.notNull<Long>()
    private var end by Delegates.notNull<Long>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_period_selection, container, false)
        btnSelectRange = view.findViewById(R.id.btnSelectRange)
        tvSelectedRange = view.findViewById(R.id.tvSelectedRange)
        btnNext = view.findViewById(R.id.btnNext)

        btnSelectRange.setOnClickListener {
            // Ограничиваем выбор дат, чтобы нельзя было выбрать прошлое
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())

            val builder = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Выберите период")
                .setCalendarConstraints(constraintsBuilder.build())
            val picker = builder.build()
            picker.show(childFragmentManager, "date_range_picker")
            picker.addOnPositiveButtonClickListener { selection ->
                if (selection.first != null && selection.second != null) {
                    start = selection.first!!
                    end = selection.second!!
                    // Рассчитываем количество дней, включая обе даты
                    val diffMillis = end - start
                    selectedPeriodDays = ((diffMillis / (1000 * 60 * 60 * 24)).toInt() + 1)
                    tvSelectedRange.text = "Период: ${picker.headerText}"
                }
            }
        }

        btnNext.setOnClickListener {
            // Если период не выбран, показываем сообщение
            if (tvSelectedRange.text.toString() == "Период не выбран") {
                Toast.makeText(requireContext(), "Выберите период", Toast.LENGTH_SHORT).show()
            } else {
                periodSelectionListener?.onPeriodSelected(selectedPeriodDays, start, end)
            }
        }
        return view
    }
}
