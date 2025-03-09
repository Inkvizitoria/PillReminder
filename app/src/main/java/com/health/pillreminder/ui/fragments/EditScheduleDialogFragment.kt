package com.health.pillreminder.ui.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.ScheduleEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EditScheduleDialogFragment(
    private val scheduleEntry: ScheduleEntry,
    private val onScheduleUpdated: () -> Unit
) : DialogFragment() {

    private lateinit var etDosage: EditText
    private lateinit var spinnerRepeatUnit: Spinner
    private lateinit var etRepeatValue: EditText
    private lateinit var btnSelectStartDate: Button
    private lateinit var btnSelectEndDate: Button
    private lateinit var btnSelectTime: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var periodStart: Long = scheduleEntry.periodStart
    private var periodEnd: Long = scheduleEntry.periodEnd
    private var scheduledTime: Long = scheduleEntry.scheduledTime

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_schedule, null)

        etDosage = view.findViewById(R.id.etDosage)
        spinnerRepeatUnit = view.findViewById(R.id.spinnerRepeatUnit)
        etRepeatValue = view.findViewById(R.id.etRepeatValue)
        btnSelectStartDate = view.findViewById(R.id.btnSelectStartDate)
        btnSelectEndDate = view.findViewById(R.id.btnSelectEndDate)
        btnSelectTime = view.findViewById(R.id.btnSelectTime)
        btnSave = view.findViewById(R.id.btnSave)
        btnCancel = view.findViewById(R.id.btnCancel)

        etDosage.setText(scheduleEntry.dosage.toString())

        val isPeriodic = scheduleEntry.repeatValue != null && scheduleEntry.repeatUnit != null
        val hasDateRange = scheduleEntry.periodStart != 0L && scheduleEntry.periodEnd != 0L

        if (isPeriodic) {
            etRepeatValue.setText(scheduleEntry.repeatValue.toString())
            spinnerRepeatUnit.setSelection(getRepeatUnitIndex(scheduleEntry.repeatUnit))
            btnSelectStartDate.text = dateFormatter.format(Date(periodStart))
            btnSelectEndDate.text = dateFormatter.format(Date(periodEnd))
            btnSelectTime.visibility = View.GONE // Для периодических графиков не редактируем время
        } else {
            etRepeatValue.visibility = View.GONE
            spinnerRepeatUnit.visibility = View.GONE
            btnSelectStartDate.visibility = if (hasDateRange) View.VISIBLE else View.GONE
            btnSelectEndDate.visibility = if (hasDateRange) View.VISIBLE else View.GONE
            btnSelectTime.text = timeFormatter.format(Date(scheduledTime))
        }

        btnSelectStartDate.setOnClickListener { showDatePicker(true) }
        btnSelectEndDate.setOnClickListener { showDatePicker(false) }
        btnSelectTime.setOnClickListener { showTimePicker() }

        btnSave.setOnClickListener { saveChanges() }
        btnCancel.setOnClickListener { dismiss() }

        return AlertDialog.Builder(requireContext())
            .setTitle("Редактирование графика")
            .setView(view)
            .create()
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val cal = Calendar.getInstance()
        val initialDate = if (isStartDate) periodStart else periodEnd
        cal.timeInMillis = initialDate

        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            cal.set(year, month, dayOfMonth)
            if (isStartDate) {
                periodStart = cal.timeInMillis
                btnSelectStartDate.text = dateFormatter.format(Date(periodStart))
            } else {
                periodEnd = cal.timeInMillis
                btnSelectEndDate.text = dateFormatter.format(Date(periodEnd))
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = scheduledTime

        TimePickerDialog(requireContext(), { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            scheduledTime = cal.timeInMillis
            btnSelectTime.text = timeFormatter.format(Date(scheduledTime))
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun saveChanges() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance().scheduleEntryDao()

            val updatedSchedule = scheduleEntry.copy(
                dosage = etDosage.text.toString().toFloat(),
                repeatValue = etRepeatValue.text.toString().toIntOrNull(),
                repeatUnit = if (etRepeatValue.text.isNotEmpty()) spinnerRepeatUnit.selectedItem.toString() else null,
                periodStart = periodStart,
                periodEnd = periodEnd,
                scheduledTime = if (etRepeatValue.visibility == View.GONE) scheduledTime else 0L
            )

            db.insert(updatedSchedule)  // Обновляем в базе

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "График обновлен", Toast.LENGTH_SHORT).show()
                onScheduleUpdated()
                dismiss()
            }
        }
    }

    private fun getRepeatUnitIndex(unit: String?): Int {
        val units = listOf("Минут", "Часов", "Дней")
        return units.indexOf(unit ?: "Дней")
    }
}
