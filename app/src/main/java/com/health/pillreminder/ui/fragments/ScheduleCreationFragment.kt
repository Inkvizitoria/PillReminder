package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.model.ReminderConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

class ScheduleCreationFragment : Fragment(R.layout.fragment_schedule_creation) {

    private lateinit var tvMedicineName: TextView
    private lateinit var rgDosageType: RadioGroup
    private lateinit var rbTablet: RadioButton
    private lateinit var rbMilligram: RadioButton
    private lateinit var etDosage: EditText
    private lateinit var tabLayoutSchedule: TabLayout
    private lateinit var viewPagerSchedule: ViewPager2
    private lateinit var btnSaveSchedule: Button

    private var dosageType: String = "Таблетка"
    private var dosageValue: Float = 0f

    private val selectedMedicines = mutableListOf<Medicine>()

    // Данные из вкладок:
    var repeatValue: Int? = null
    var repeatUnit: String? = null
    var periodStart: Long = 0L
    var periodEnd: Long = 0L
    var singleTimestamp: Long = 0L

    companion object {
        fun newInstance(selectedMedicines: List<Medicine>): ScheduleCreationFragment {
            val fragment = ScheduleCreationFragment()
            val bundle = Bundle()
            bundle.putSerializable("selectedMedicines", ArrayList(selectedMedicines) as Serializable)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val meds = arguments?.getSerializable("selectedMedicines") as? ArrayList<Medicine>
        if (meds != null) {
            selectedMedicines.clear()
            selectedMedicines.addAll(meds)
            Log.d("ScheduleCreation", "Получено лекарств: ${selectedMedicines.size}")
        } else {
            Log.w("ScheduleCreation", "Не получен список лекарств")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvMedicineName = view.findViewById(R.id.tvMedicineName)
        rgDosageType = view.findViewById(R.id.rgDosageType)
        rbTablet = view.findViewById(R.id.rbTablet)
        rbMilligram = view.findViewById(R.id.rbMilligram)
        etDosage = view.findViewById(R.id.etDosage)
        tabLayoutSchedule = view.findViewById(R.id.tabLayoutSchedule)
        viewPagerSchedule = view.findViewById(R.id.viewPagerSchedule)
        btnSaveSchedule = view.findViewById(R.id.btnSaveSchedule)

        val adapter = SchedulePagerAdapter(this)
        viewPagerSchedule.adapter = adapter

        if (selectedMedicines.isNotEmpty()) {
            val medicineNames = selectedMedicines.joinToString(", ") { it.name }
            tvMedicineName.text = "График для: $medicineNames"
        } else {
            tvMedicineName.text = "Нет выбранных лекарств"
        }

        TabLayoutMediator(tabLayoutSchedule, viewPagerSchedule) { tab, position ->
            when (position) {
                0 -> tab.text = "Периодичный"
                1 -> tab.text = "Конкретный"
            }
        }.attach()

        rgDosageType.setOnCheckedChangeListener { _, checkedId ->
            dosageType = if (checkedId == R.id.rbTablet) "Таблетка" else "Миллиграмм"
        }

        btnSaveSchedule.setOnClickListener {
            val dosageStr = etDosage.text.toString().trim()
            if (dosageStr.isNotEmpty()) {
                dosageValue = dosageStr.toFloatOrNull() ?: 0f
            }

            val currentTab = viewPagerSchedule.currentItem
            if (currentTab == 0) {
                if (repeatValue == null || repeatUnit == null || periodStart <= 0L || periodEnd <= 0L) {
                    Toast.makeText(requireContext(), "Не все поля периодичного заполнены", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                saveSchedules(periodic = true)
            } else {
                if (singleTimestamp <= 0L) {
                    Toast.makeText(requireContext(), "Не указано конкретное время", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                saveSchedules(periodic = false)
            }
        }
    }

    private fun saveSchedules(periodic: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance()
                val scheduleDao = db.scheduleEntryDao()

                selectedMedicines.forEach { medicine ->
                    val newSchedule = if (periodic) {
                        ScheduleEntry(
                            medicineId = medicine.id,
                            name = medicine.name,
                            periodStart = periodStart,
                            periodEnd = periodEnd,
                            dosage = dosageValue,
                            dosageUnit = dosageType,
                            scheduledTime = 0L,
                            repeatValue = repeatValue,
                            repeatUnit = repeatUnit,
                            reminderConfig = ReminderConfig(advanceMinutes = 10, repeatTimes = listOf(0, 10))
                        )
                    } else {
                        ScheduleEntry(
                            medicineId = medicine.id,
                            name = medicine.name,
                            scheduledTime = singleTimestamp,
                            periodStart = singleTimestamp,
                            periodEnd = singleTimestamp,
                            dosage = dosageValue,
                            dosageUnit = dosageType,
                            repeatValue = null,
                            repeatUnit = null,
                            reminderConfig = null
                        )
                    }
                    scheduleDao.insert(newSchedule)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        if (periodic) "Периодичный график сохранён" else "График на конкретный день сохранён",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Очищаем состояние перед возвратом
                    resetState()

                    // Возвращаемся к выбору лекарств
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MedicineSelectionForScheduleFragment())
                        .commit()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка сохранения графика: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("ScheduleCreation", "Ошибка сохранения графика", e)
                }
            }
        }
    }

    private fun resetState() {
        selectedMedicines.clear()
        dosageValue = 0f
        repeatValue = null
        repeatUnit = null
        periodStart = 0L
        periodEnd = 0L
        singleTimestamp = 0L
    }


}
