package com.health.pillreminder.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.model.ReminderConfig
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.ui.fragments.AddMoreFragment
import com.health.pillreminder.ui.fragments.MedicineConfigurationFragment
import com.health.pillreminder.ui.fragments.MedicineSelectionFragment
import com.health.pillreminder.ui.fragments.PeriodSelectionFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduleWizardActivity : AppCompatActivity(),
    MedicineSelectionFragment.SelectionListener,
    MedicineConfigurationFragment.ConfigurationListener,
    PeriodSelectionFragment.PeriodSelectionListener,
    AddMoreFragment.AddMoreListener {

    // Список выбранных лекарств для текущего мастера
    private val selectedMedicines = mutableListOf<Medicine>()
    // Собранные записи расписания (без периода, который задаётся отдельно)
    private val scheduleEntries = mutableListOf<ScheduleEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_schedule_wizard_container)
        // Запускаем первый шаг: выбор лекарств
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MedicineSelectionFragment())
                .commit()
        }
    }

    // --- Callback из MedicineSelectionFragment ---
    override fun onMedicinesSelected(medicines: List<Medicine>) {
        selectedMedicines.clear()
        selectedMedicines.addAll(medicines)
        if (selectedMedicines.isNotEmpty()) {
            // Переходим к настройке для первого выбранного лекарства
            val fragment = MedicineConfigurationFragment.newInstance(selectedMedicines[0])
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            Toast.makeText(this, "Ничего не выбрано", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Callback из MedicineConfigurationFragment ---
    override fun onConfigurationSet(
        medicine: Medicine,
        dosage: Float,
        dosageUnit: String,
        scheduledTime: Long,
        repeatValue: Int?,
        repeatUnit: String?
    ) {
        // Создаем запись расписания для текущего лекарства (период зададим позже)
        val entry = ScheduleEntry(
            medicineId = medicine.id,
            name = medicine.name,
            dosage = dosage,
            dosageUnit = dosageUnit,
            scheduledTime = scheduledTime,
            repeatValue = repeatValue,
            repeatUnit = repeatUnit,
            periodStart = 0L,
            periodEnd = 0L,
            reminderConfig = if (repeatValue != null && repeatValue > 0) {
                ReminderConfig(
                    advanceMinutes = 10,
                    repeatTimes = listOf(0, 10),
                    repeatValue = repeatValue,
                    repeatUnit = repeatUnit
                )
            } else {
                ReminderConfig(advanceMinutes = 10, repeatTimes = listOf(0, 10))
            }
        )
        scheduleEntries.add(entry)
        // Найдем индекс текущего лекарства в списке
        val currentIndex = selectedMedicines.indexOfFirst { it.id == medicine.id }
        if (currentIndex != -1 && currentIndex < selectedMedicines.size - 1) {
            // Если есть следующее лекарство, запускаем его настройку
            val nextMedicine = selectedMedicines[currentIndex + 1]
            val fragment = MedicineConfigurationFragment.newInstance(nextMedicine)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            // Если все лекарства настроены, переходим к выбору периода графика
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PeriodSelectionFragment())
                .commit()
        }
    }

    // --- Callback из PeriodSelectionFragment ---
    override fun onPeriodSelected(periodDays: Int, start: Long, end: Long) {
        // Обновляем все записи расписания, устанавливая период
        scheduleEntries.replaceAll { it.copy(periodStart = start, periodEnd = end) }
        // Переходим к шагу "Добавить ещё?"
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddMoreFragment())
            .commit()
    }

    // --- Callback из AddMoreFragment ---
    override fun onAddMoreResult(addMore: Boolean) {
        if (addMore) {
            // Если пользователь хочет добавить ещё, перезапускаем мастер
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MedicineSelectionFragment())
                .commit()
        } else {
            // Иначе сохраняем график в БД
            CoroutineScope(Dispatchers.IO).launch {
                scheduleEntries.forEach { entry ->
                    AppDatabase.getInstance().scheduleEntryDao().insert(entry)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ScheduleWizardActivity, "График сохранён", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
