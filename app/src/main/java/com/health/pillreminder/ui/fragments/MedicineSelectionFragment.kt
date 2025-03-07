package com.health.pillreminder.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.Medicine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicineSelectionFragment : Fragment() {

    interface SelectionListener {
        fun onMedicinesSelected(medicines: List<Medicine>)
    }

    private lateinit var listView: ListView
    private lateinit var btnNext: Button
    private var medicines = listOf<Medicine>()
    private val selectedMedicines = mutableListOf<Medicine>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_medicine_selection, container, false)
        listView = view.findViewById(R.id.listViewMedicines)
        btnNext = view.findViewById(R.id.btnNext)
        btnNext.setOnClickListener {
            if (selectedMedicines.isEmpty()) {
                Toast.makeText(requireContext(), "Выберите хотя бы одно лекарство", Toast.LENGTH_SHORT).show()
            } else {
                (parentFragment as? SelectionListener)?.onMedicinesSelected(selectedMedicines)
            }
        }
        loadMedicines()
        return view
    }

    private fun loadMedicines() {
        CoroutineScope(Dispatchers.IO).launch {
            medicines = AppDatabase.getInstance().medicineDao().getAllMedicines()
            withContext(Dispatchers.Main) {
                val adapter = MedicineArrayAdapter(requireContext(), medicines, selectedMedicines)
                listView.adapter = adapter
            }
        }
    }
}

// Простой ArrayAdapter с CheckBox (MedicineArrayAdapter.kt)
class MedicineArrayAdapter(
    context: Context,
    private val medicines: List<Medicine>,
    private val selectedMedicines: MutableList<Medicine>
) : android.widget.ArrayAdapter<Medicine>(context, R.layout.item_medicine_selection, medicines) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_medicine_selection, parent, false)
        val medicine = medicines[position]
        val tvName = view.findViewById<TextView>(R.id.tvMedicineName)
        val checkBox = view.findViewById<CheckBox>(R.id.cbSelect)
        tvName.text = medicine.name
        checkBox.isChecked = selectedMedicines.contains(medicine)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!selectedMedicines.contains(medicine)) selectedMedicines.add(medicine)
            } else {
                selectedMedicines.remove(medicine)
            }
        }
        return view
    }
}
