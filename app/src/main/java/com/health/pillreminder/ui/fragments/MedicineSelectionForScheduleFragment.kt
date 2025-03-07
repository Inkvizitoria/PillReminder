package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.Medicine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicineSelectionForScheduleFragment : Fragment(R.layout.fragment_medicine_selection_for_schedule) {

    interface OnMedicinesSelectedListener {
        fun onMedicinesSelectedForSchedule(medicines: List<Medicine>)
    }

    private lateinit var recyclerMedicineList: RecyclerView
    private lateinit var btnNext: Button
    private val adapter = MedicineSelectionAdapter() // некий адаптер для списка

    var listener: OnMedicinesSelectedListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerMedicineList = view.findViewById(R.id.recyclerMedicineList)
        btnNext = view.findViewById(R.id.btnNext)

        recyclerMedicineList.layoutManager = LinearLayoutManager(requireContext())
        recyclerMedicineList.adapter = adapter

        // Загрузим список лекарств из БД (пример)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val medicines = AppDatabase.getInstance().medicineDao().getAllMedicines() // пример DAO
            withContext(Dispatchers.Main) {
                adapter.submitList(medicines)
            }
        }

        btnNext.setOnClickListener {
            val selected = adapter.getSelectedMedicines() // метод адаптера
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), "Выберите хотя бы одно лекарство", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Передаём выбранные лекарства наружу
            listener?.onMedicinesSelectedForSchedule(selected)
        }
    }
}
