package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.ui.viewmodel.ScheduleSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicineSelectionForScheduleFragment : Fragment(R.layout.fragment_medicine_selection_for_schedule) {

    interface OnMedicinesSelectedListener {
        fun onMedicinesSelectedForSchedule(medicines: List<Medicine>)
    }

    private lateinit var recyclerMedicineList: RecyclerView
    private lateinit var btnNext: Button
    private lateinit var tvTitleSelectMedicine: TextView
    private lateinit var tvEmptyMedicineInstructions: TextView
    private val adapter = MedicineSelectionAdapter()

    var listener: OnMedicinesSelectedListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerMedicineList = view.findViewById(R.id.recyclerMedicineList)
        btnNext = view.findViewById(R.id.btnNext)
        tvTitleSelectMedicine = view.findViewById(R.id.tvTitleSelectMedicine)
        tvEmptyMedicineInstructions = view.findViewById(R.id.tvEmptyMedicineInstructions)

        recyclerMedicineList.layoutManager = LinearLayoutManager(requireContext())
        recyclerMedicineList.adapter = adapter

        loadMedicines()

        btnNext.setOnClickListener {
            val selectedMedicines = adapter.getSelectedMedicines()
            if (selectedMedicines.isEmpty()) {
                ToastUtils.showCustomToast(requireContext(), "Выберите хотя бы одно лекарство", ToastType.INFO)
                return@setOnClickListener
            }
            val viewModel = ViewModelProvider(requireActivity()).get(ScheduleSharedViewModel::class.java)

            // ✅ Очищаем ViewModel перед переходом
            viewModel.clearData()

            val fragment = ScheduleCreationFragment.newInstance(selectedMedicines)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

    }

    private fun loadMedicines() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val medicines = AppDatabase.getInstance().medicineDao().getAllMedicines()
            withContext(Dispatchers.Main) {
                val isEmpty = medicines.isEmpty()

                // Если лекарств нет, показываем инструкцию, скрываем заголовок и RecyclerView
                tvEmptyMedicineInstructions.visibility = if (isEmpty) View.VISIBLE else View.GONE
                tvTitleSelectMedicine.visibility = if (isEmpty) View.GONE else View.VISIBLE
                recyclerMedicineList.visibility = if (isEmpty) View.GONE else View.VISIBLE
                btnNext.visibility = if (isEmpty) View.GONE else View.VISIBLE

                adapter.submitList(medicines)
            }
        }
    }
}
