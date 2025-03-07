package com.health.pillreminder.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.entities.Medicine

class MedicineSelectionAdapter : RecyclerView.Adapter<MedicineSelectionAdapter.ViewHolder>() {

    private val items = mutableListOf<Medicine>()
    private val selectedIds = mutableSetOf<Long>() // набор id выбранных лекарств

    fun submitList(medicines: List<Medicine>) {
        items.clear()
        items.addAll(medicines)
        notifyDataSetChanged()
    }

    fun getSelectedMedicines(): List<Medicine> {
        return items.filter { selectedIds.contains(it.id) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicine_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxMedicine)
        fun bind(medicine: Medicine) {
            checkBox.text = medicine.name
            checkBox.isChecked = selectedIds.contains(medicine.id)
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedIds.add(medicine.id)
                } else {
                    selectedIds.remove(medicine.id)
                }
            }
        }
    }
}
