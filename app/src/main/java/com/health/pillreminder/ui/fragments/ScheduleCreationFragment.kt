package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.data.model.ReminderConfig
import com.health.pillreminder.ui.dialogs.ColorPickerDialogFragment
import com.health.pillreminder.ui.viewmodel.ScheduleSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.util.*

class ScheduleCreationFragment : Fragment(R.layout.fragment_schedule_creation) {

    private lateinit var tvMedicineName: TextView
    private lateinit var rgDosageType: RadioGroup
    private lateinit var rbTablet: RadioButton
    private lateinit var rbMilligram: RadioButton
    private lateinit var etDosage: EditText
    private lateinit var tabLayoutSchedule: TabLayout
    private lateinit var viewPagerSchedule: ViewPager2
    private lateinit var btnSaveSchedule: Button
    private lateinit var btnPickColor: Button
    private lateinit var viewModel: ScheduleSharedViewModel

    private var dosageType: String = "Таблетка"
    private var dosageValue: Float = 0f

    private val selectedMedicines = mutableListOf<Medicine>()
    private var currentMedicineIndex = 0

    // Данные для периодичного графика
    var repeatValue: Int? = null
    var repeatUnit: String? = null
    var periodStart: Long = 0L
    var periodEnd: Long = 0L

    // Данные для конкретного графика:
    var singleTimestamp: Long = 0L  // для варианта "Конкретная дата"
    var fixedTime: Long = 0L  // для варианта "Период с дат"

    // Активный период (для периодичного графика)
    var activeStartTime: Long = 0L     // 00:00
    var activeEndTime: Long = 23 * 60 * 60 * 1000L + 59 * 60 * 1000L  // 23:59

    // Цвет блока, выбранный через ColorPickerDialogFragment
    private var blockColorKey: String = "blue"

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
        viewModel = ViewModelProvider(requireActivity()).get(ScheduleSharedViewModel::class.java)

        val meds = arguments?.getSerializable("selectedMedicines") as? ArrayList<Medicine>
        if (meds != null) {
            selectedMedicines.clear()
            selectedMedicines.addAll(meds)
            currentMedicineIndex = 0
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
        btnPickColor = view.findViewById(R.id.btnPickColor)

        viewModel.singleTimestamp.observe(viewLifecycleOwner) { timestamp ->
            Log.d("ScheduleCreation", "Получен singleTimestamp: $timestamp")
            singleTimestamp = timestamp
        }

        viewModel.periodStart.observe(viewLifecycleOwner) { start ->
            Log.d("ScheduleCreation", "Получен periodStart: $start")
            periodStart = start
        }

        viewModel.periodEnd.observe(viewLifecycleOwner) { end ->
            Log.d("ScheduleCreation", "Получен periodEnd: $end")
            periodEnd = end
        }

        viewModel.fixedTime.observe(viewLifecycleOwner) { time ->
            Log.d("ScheduleCreation", "Получен fixedTime: $time")
            fixedTime = time
        }

        updateMedicineName()

        btnPickColor.setOnClickListener {
            val dialog = ColorPickerDialogFragment()
            dialog.listener = object : ColorPickerDialogFragment.ColorPickerListener {
                override fun onColorPicked(colorKey: String) {
                    blockColorKey = colorKey
                    btnPickColor.text = "Цвет: $colorKey"
                }
                override fun onColorPickCancelled() {
                    blockColorKey = "blue"
                    btnPickColor.text = "Цвет: blue (дефолт)"
                }
            }
            dialog.show(childFragmentManager, "ColorPickerDialog")
        }

        val pagerAdapter = SchedulePagerAdapter(this)
        viewPagerSchedule.adapter = pagerAdapter
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

            if (dosageValue === 0f) {
                ToastUtils.showCustomToast(requireContext(), "Не указана дозировка", ToastType.ERROR)
                return@setOnClickListener
            }

            val currentTab = viewPagerSchedule.currentItem
            if (currentTab == 0) {
                // Периодичный график
                if (repeatValue == null || repeatUnit == null || periodStart <= 0L || periodEnd <= 0L) {
                    ToastUtils.showCustomToast(requireContext(), "Не все поля периодичного заполнены", ToastType.ERROR)
                    return@setOnClickListener
                }
                saveSchedule(periodic = true)
            } else {
                // Проверяем, что данные конкретного графика корректно передаются
                if (singleTimestamp > 0L) {
                    saveSchedule(specificPeriod = false)
                } else if (periodStart > 0L && periodEnd > 0L && fixedTime > 0L) {
                    saveSchedule(specificPeriod = true)
                } else {
                    Log.e("ERROR", "singleTimestamp: $singleTimestamp, periodStart: $periodStart, periodEnd: $periodEnd, fixedTime: $fixedTime")
                    ToastUtils.showCustomToast(requireContext(), "Не указано конкретное время", ToastType.ERROR)
                    return@setOnClickListener
                }
            }
        }

    }

    private fun updateMedicineName() {
        if (selectedMedicines.isNotEmpty() && currentMedicineIndex in selectedMedicines.indices) {
            val currentMedicine = selectedMedicines[currentMedicineIndex]
            tvMedicineName.text = "График для: ${currentMedicine.name}"
        } else {
            tvMedicineName.text = "Нет выбранных лекарств"
        }
    }

    /**
     * Сохраняет график в зависимости от режима.
     * @param periodic true – периодичный график; false – конкретный график.
     * Для конкретного графика, если specificPeriod==true, используется период с дат и fixedTime,
     * иначе используется singleTimestamp.
     */
    private fun saveSchedule(periodic: Boolean = false, specificPeriod: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance()
                val scheduleDao = db.scheduleEntryDao()
                val medicine = selectedMedicines[currentMedicineIndex]
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
                        reminderConfig = ReminderConfig(advanceMinutes = 10, repeatTimes = listOf(0, 10)),
                        blockColor = blockColorKey,
                        isDeleted = false,
                        activeStartTime = activeStartTime,
                        activeEndTime = activeEndTime
                    )
                } else {
                    if (specificPeriod) {
                        // Вариант "Период с дат" для конкретного графика:
                        // Фиксированное время применяется к каждому дню выбранного периода.
                        // Здесь scheduledTime вычисляем как periodStart + fixedTime.
                        ScheduleEntry(
                            medicineId = medicine.id,
                            name = medicine.name,
                            periodStart = periodStart,
                            periodEnd = periodEnd,
                            dosage = dosageValue,
                            dosageUnit = dosageType,
                            scheduledTime = periodStart + fixedTime,
                            repeatValue = null,
                            repeatUnit = null,
                            reminderConfig = null,
                            blockColor = blockColorKey,
                            isDeleted = false,
                            activeStartTime = fixedTime,
                            activeEndTime = fixedTime
                        )
                    } else {
                        // Вариант "Конкретная дата"
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
                            reminderConfig = null,
                            blockColor = blockColorKey,
                            isDeleted = false,
                            activeStartTime = 0,
                            activeEndTime = 0
                        )
                    }
                }
                scheduleDao.insert(newSchedule)
                withContext(Dispatchers.Main) {
                    ToastUtils.showCustomToast(requireContext(),
                        if (periodic) "Периодичный график сохранён"
                        else if (specificPeriod) "График (период с дат) сохранён"
                        else "График на конкретный день сохранён",
                        ToastType.SUCCESS)
                    moveToNextMedicineOrFinish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    ToastUtils.showCustomToast(requireContext(), "Ошибка сохранения графика: ${e.message}",  ToastType.ERROR)
                    Log.e("ScheduleCreation", "Ошибка сохранения графика", e)
                }
            }
        }
    }

    private fun moveToNextMedicineOrFinish() {
        currentMedicineIndex++
        if (currentMedicineIndex < selectedMedicines.size) {
            etDosage.text.clear()
            repeatValue = null
            repeatUnit = null
            periodStart = 0L
            periodEnd = 0L
            singleTimestamp = 0L
            fixedTime = 0L
            activeStartTime = 0L
            activeEndTime = 23 * 60 * 60 * 1000L + 59 * 60 * 1000L

            // ✅ Очистка данных в ViewModel
            viewModel.clearData()

            updateMedicineName()
            ToastUtils.showCustomToast(requireContext(), "Настройте график для следующего лекарства",  ToastType.INFO)
        } else {
            ToastUtils.showCustomToast(requireContext(), "Все графики созданы",  ToastType.SUCCESS)

            // ✅ После очистки корректно переходим обратно
            viewModel.clearData()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MedicineSelectionForScheduleFragment())
                .commit()
        }
    }

}
