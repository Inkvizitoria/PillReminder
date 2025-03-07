package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.model.ReminderConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ScheduleFragment : Fragment() {

    private lateinit var spinnerMedicine: Spinner
    private lateinit var editTime: EditText
    private lateinit var editDosage: EditText
    private lateinit var spinnerDosageUnit: Spinner
    private lateinit var editRepeatValue: EditText
    private lateinit var spinnerRepeatUnit: Spinner
    private lateinit var btnSchedule: Button

    // Список лекарств, загруженных из БД
    private var medicines: List<com.health.pillreminder.data.entities.Medicine> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        spinnerMedicine = view.findViewById(R.id.spinner_medicine)
        editTime = view.findViewById(R.id.edit_time)
        editDosage = view.findViewById(R.id.edit_dosage)
        spinnerDosageUnit = view.findViewById(R.id.spinner_dosage_unit)
        editRepeatValue = view.findViewById(R.id.edit_repeat_value)
        spinnerRepeatUnit = view.findViewById(R.id.spinner_repeat_unit)
        btnSchedule = view.findViewById(R.id.btn_schedule)

        loadMedicines()

        btnSchedule.setOnClickListener {
            scheduleMedicine()
        }

        return view
    }

    private fun loadMedicines() {
        CoroutineScope(Dispatchers.IO).launch {
            // Получаем список лекарств (активных) из базы данных
            medicines = AppDatabase.getInstance().medicineDao().getAllMedicines()
            withContext(Dispatchers.Main) {
                // Заполняем Spinner именами лекарств
                val medicineNames = medicines.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, medicineNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerMedicine.adapter = adapter
            }
        }
    }

    private fun scheduleMedicine() {
        val selectedPosition = spinnerMedicine.selectedItemPosition
        if (selectedPosition < 0 || selectedPosition >= medicines.size) return

        val selectedMedicine = medicines[selectedPosition]
        val timeStr = editTime.text.toString().trim()
        val dosageStr = editDosage.text.toString().trim()
        if (timeStr.isEmpty() || dosageStr.isEmpty()) return

        // Парсинг времени в формате "HH:mm"
        val parts = timeStr.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        val scheduledTime = calendar.timeInMillis

        val dosage = dosageStr.toFloatOrNull() ?: return

        // Чтение параметров повторения (если введено)
        val repeatValueText = editRepeatValue.text.toString().trim()
        val repeatValue = repeatValueText.toIntOrNull()
        val repeatUnit = spinnerRepeatUnit.selectedItem?.toString() ?: ""

        // Формирование ReminderConfig с параметрами повторения, если они заданы
        val reminderConfig = if (repeatValue != null && repeatValue > 0) {
            ReminderConfig(
                advanceMinutes = 10,
                repeatTimes = listOf(0, 10),
                repeatValue = repeatValue,
                repeatUnit = repeatUnit
            )
        } else {
            ReminderConfig(advanceMinutes = 10, repeatTimes = listOf(0, 10))
        }

        // Здесь период задаётся как 1 день; при необходимости можно добавить выбор периода
        val periodStart = System.currentTimeMillis()
        val periodEnd = periodStart + 24 * 60 * 60 * 1000L

        // Создаём объект ScheduleEntry
        // Создаём объект ScheduleEntry, учитывая новые параметры repeatValue и repeatUnit
        val scheduleEntry = ScheduleEntry(
            medicineId = selectedMedicine.id,
            scheduledTime = scheduledTime,
            dosage = dosage,
            dosageUnit = spinnerDosageUnit.selectedItem.toString(),
            repeatValue = repeatValue, // передаём повторное значение (или null)
            repeatUnit = if (repeatValue != null) repeatUnit else null, // передаём единицу повторения, если задано
            periodStart = periodStart,
            periodEnd = periodEnd,
            reminderConfig = reminderConfig,
            name = selectedMedicine.name
        )


        // Сохраняем запись расписания в БД
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance().scheduleEntryDao().insert(scheduleEntry)
            withContext(Dispatchers.Main) {
                // Можно вывести уведомление
                // Toast.makeText(requireContext(), "Расписание сохранено", Toast.LENGTH_SHORT).show()
                clearFields()
            }
        }
    }

    private fun clearFields() {
        editTime.text.clear()
        editDosage.text.clear()
        editRepeatValue.text.clear()
        // Если нужно, можно сбросить значения спиннеров к значениям по умолчанию:
        spinnerDosageUnit.setSelection(0)
        spinnerRepeatUnit.setSelection(0)
    }
}
