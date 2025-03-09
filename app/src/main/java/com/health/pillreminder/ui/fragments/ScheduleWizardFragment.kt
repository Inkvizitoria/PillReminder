package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.data.model.ReminderConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ScheduleWizardFragment : Fragment(R.layout.fragment_schedule_wizard_container),
    MedicineSelectionFragment.SelectionListener,
    MedicineConfigurationFragment.ConfigurationListener,
    PeriodSelectionFragment.PeriodSelectionListener,
    AddMoreFragment.AddMoreListener {

    // Список выбранных лекарств
    private val selectedMedicines = mutableListOf<Medicine>()
    // Список настроек расписания
    private val scheduleEntries = mutableListOf<ScheduleEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Начинаем флоу с выбора лекарств
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.schedule_wizard_container, MedicineSelectionFragment())
                .commit()
        }
    }

    // Callback из MedicineSelectionFragment
    override fun onMedicinesSelected(medicines: List<Medicine>) {
        selectedMedicines.clear()
        selectedMedicines.addAll(medicines)
        if (selectedMedicines.isNotEmpty()) {
            // Загружаем настройку для первого выбранного лекарства
            val configFragment = MedicineConfigurationFragment.newInstance(selectedMedicines[0])
            configFragment.configurationListener = this
            childFragmentManager.beginTransaction()
                .replace(R.id.schedule_wizard_container, configFragment)
                .commit()
        } else {
            ToastUtils.showCustomToast(requireContext(), "Ни одно лекарство не выбрано", ToastType.INFO)
        }
    }

    // Callback из MedicineConfigurationFragment
    override fun onConfigurationSet(
        medicine: Medicine,
        dosage: Float,
        dosageUnit: String,
        scheduledTime: Long,
        repeatValue: Int?,
        repeatUnit: String?
    ) {
        // Создаем запись расписания для данного лекарства
        val entry = ScheduleEntry(
            medicineId = medicine.id,
            dosage = dosage,
            dosageUnit = dosageUnit,
            scheduledTime = scheduledTime,
            repeatValue = repeatValue,
            repeatUnit = if (repeatValue != null && repeatValue > 0) repeatUnit else null,
            periodStart = 0L, // заполнят позже
            periodEnd = 0L,
            name = medicine.name,
            reminderConfig = if (repeatValue != null && repeatValue > 0)
                ReminderConfig(
                    advanceMinutes = 10,
                    repeatTimes = listOf(0, 10),
                    repeatValue = repeatValue,
                    repeatUnit = repeatUnit
                )
            else
                ReminderConfig(advanceMinutes = 10, repeatTimes = listOf(0, 10))
        )
        scheduleEntries.add(entry)
        // Если текущее лекарство не последнее, переходим к следующему, иначе – к выбору периода
        val currentIndex = selectedMedicines.indexOfFirst { it.id == medicine.id }
        if (currentIndex != -1 && currentIndex < selectedMedicines.size - 1) {
            val nextMedicine = selectedMedicines[currentIndex + 1]
            val nextFragment = MedicineConfigurationFragment.newInstance(nextMedicine)
            nextFragment.configurationListener = this
            childFragmentManager.beginTransaction()
                .replace(R.id.schedule_wizard_container, nextFragment)
                .commit()
        } else {
            // Создаем экземпляр PeriodSelectionFragment и устанавливаем слушатель
            val periodFragment = PeriodSelectionFragment()
            periodFragment.periodSelectionListener = this
            childFragmentManager.beginTransaction()
                .replace(R.id.schedule_wizard_container, periodFragment)
                .commit()
        }
    }

    // Callback из PeriodSelectionFragment
    override fun onPeriodSelected(periodDays: Int, start: Long, end: Long) {
        scheduleEntries.replaceAll { it.copy(periodStart = start, periodEnd = end) }

        // Создаем экземпляр AddMoreFragment и устанавливаем listener
        val addMoreFragment = AddMoreFragment()
        addMoreFragment.addMoreListener = this  // ScheduleWizardFragment должен реализовать AddMoreListener
        childFragmentManager.beginTransaction()
            .replace(R.id.schedule_wizard_container, addMoreFragment)
            .commit()
    }

    // Callback из AddMoreFragment
    override fun onAddMoreResult(addMore: Boolean) {
        if (addMore) {
            // Если пользователь хочет добавить ещё, переходим к выбору лекарств снова
            childFragmentManager.beginTransaction()
                .replace(R.id.schedule_wizard_container, MedicineSelectionFragment())
                .commit()
        } else {
            saveSchedule()
        }
    }

    private fun saveSchedule() {
        // Сохраняем все записи расписания в базе данных
        lifecycleScope.launch(Dispatchers.IO) {
            scheduleEntries.forEach { entry ->
                AppDatabase.getInstance().scheduleEntryDao().insert(entry)
            }
            withContext(Dispatchers.Main) {
                ToastUtils.showCustomToast(requireContext(), "График сохранён", ToastType.ERROR)
                // Завершаем флоу, например, возвращаемся к главному экрану
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }
}
