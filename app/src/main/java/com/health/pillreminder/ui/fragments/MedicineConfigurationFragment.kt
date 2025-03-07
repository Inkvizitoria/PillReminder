package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.health.pillreminder.R
import com.health.pillreminder.data.entities.Medicine
import java.util.*

class MedicineConfigurationFragment : Fragment() {

    interface ConfigurationListener {
        fun onConfigurationSet(
            medicine: Medicine,
            dosage: Float,
            dosageUnit: String,
            scheduledTime: Long,
            repeatValue: Int?,
            repeatUnit: String?
        )
    }

    var configurationListener: ConfigurationListener? = null

    private lateinit var etDosage: EditText
    private lateinit var spinnerDosageUnit: Spinner
    private lateinit var etTime: EditText
    private lateinit var etRepeatValue: EditText
    private lateinit var spinnerRepeatUnit: Spinner
    private lateinit var btnNext: Button

    private var medicine: Medicine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем данные о лекарстве из аргументов
        arguments?.let {
            medicine = Medicine(
                id = it.getLong(ARG_MEDICINE_ID),
                name = it.getString(ARG_MEDICINE_NAME) ?: "",
                description = it.getString(ARG_MEDICINE_DESCRIPTION)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine_configuration, container, false)
        etDosage = view.findViewById(R.id.etDosage)
        spinnerDosageUnit = view.findViewById(R.id.spinnerDosageUnit)
        etTime = view.findViewById(R.id.etTime)
        etRepeatValue = view.findViewById(R.id.etRepeatValue)
        spinnerRepeatUnit = view.findViewById(R.id.spinnerRepeatUnit)
        btnNext = view.findViewById(R.id.btnNext)

        // Устанавливаем адаптер для спиннера дозировки (если не задан в XML)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.dosage_units,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDosageUnit.adapter = adapter
        }

        // Аналогично для спиннера повторения
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.repeat_units,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRepeatUnit.adapter = adapter
        }

        btnNext.setOnClickListener {
            val dosage = etDosage.text.toString().toFloatOrNull() ?: 0f
            val dosageUnit = spinnerDosageUnit.selectedItem.toString()
            val timeStr = etTime.text.toString().trim()
            val repeatValue = etRepeatValue.text.toString().toIntOrNull()

            // Если дозировка не задана или равна 0, выводим ошибку
            if (dosage <= 0f) {
                Toast.makeText(requireContext(), "Введите корректную дозировку", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Проверяем, что либо введено время, либо задан интервал повторения (repeatValue > 0)
            if (timeStr.isEmpty() && (repeatValue == null || repeatValue <= 0)) {
                Toast.makeText(requireContext(), "Введите время приема или интервал повторения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Если введено время, парсим его, иначе - используем 0 (или другое значение, если используется повторение)
            val scheduledTime = if (timeStr.isNotEmpty()) parseTimeToMillis(timeStr) else 0L
            val repeatUnit = spinnerRepeatUnit.selectedItem?.toString()

            medicine?.let { med ->
                configurationListener?.onConfigurationSet(med, dosage, dosageUnit, scheduledTime, repeatValue, repeatUnit)
            }
        }


        return view
    }

    private fun parseTimeToMillis(timeStr: String): Long {
        // Простой парсер времени в формате "HH:mm"
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis
    }

    companion object {
        private const val ARG_MEDICINE_ID = "medicine_id"
        private const val ARG_MEDICINE_NAME = "medicine_name"
        private const val ARG_MEDICINE_DESCRIPTION = "medicine_description"

        fun newInstance(medicine: Medicine): MedicineConfigurationFragment {
            val fragment = MedicineConfigurationFragment()
            val args = Bundle()
            args.putLong(ARG_MEDICINE_ID, medicine.id)
            args.putString(ARG_MEDICINE_NAME, medicine.name)
            args.putString(ARG_MEDICINE_DESCRIPTION, medicine.description)
            fragment.arguments = args
            return fragment
        }
    }
}
