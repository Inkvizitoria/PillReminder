package com.health.pillreminder.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.health.pillreminder.R
import com.health.pillreminder.data.entities.Medicine

class MedicineMultiSelectAdapter(
    private var medicines: List<Medicine>
) : RecyclerView.Adapter<MedicineMultiSelectAdapter.ViewHolder>() {

    // Свойство, определяющее, отображать ли кнопку редактирования
    var showEditButton: Boolean = true

    // Для хранения выбранных элементов по id
    private val selectedItems = mutableSetOf<Long>()

    // Callback для обычного клика по элементу
    var onItemClick: ((Medicine) -> Unit)? = null
    // Callback для долгого клика или клика по чекбоксу, чтобы запустить Action Mode
    var onItemLongClick: ((Medicine) -> Unit)? = null
    // Callback для клика по кнопке редактирования
    var onEditClick: ((Medicine) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_medicine_name)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_medicine_description)
        //val checkBox: MaterialCheckBox = itemView.findViewById(R.id.checkbox_select)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)

        init {
            itemView.setOnClickListener {
                if (!multiSelectMode) {
                    onItemClick?.invoke(medicines[adapterPosition])
                } else {
                    toggleSelection(adapterPosition)
                    onItemLongClick?.invoke(medicines[adapterPosition])
                }
            }
/*            checkBox.setOnClickListener {
                println(multiSelectMode)
                if (!multiSelectMode) {
                    multiSelectMode = true
                    onItemLongClick?.invoke(medicines[adapterPosition])
                }
                toggleSelection(adapterPosition)
            }*/
            btnEdit.setOnClickListener {
                onEditClick?.invoke(medicines[adapterPosition])
            }/*
            itemView.setOnLongClickListener {
                if (!multiSelectMode) {
                    multiSelectMode = true
                    onItemLongClick?.invoke(medicines[adapterPosition])
                }
                toggleSelection(adapterPosition)
                true
            }*/
        }
    }

    var multiSelectMode: Boolean = true // В нашем случае чекбоксы всегда видны

    private fun toggleSelection(position: Int) {
        val item = medicines[position]
        if (selectedItems.contains(item.id)) {
            selectedItems.remove(item.id)
        } else {
            selectedItems.add(item.id)
        }
        notifyItemChanged(position)
    }

    fun getSelectedItems(): List<Medicine> = medicines.filter { selectedItems.contains(it.id) }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun updateData(newMedicines: List<Medicine>) {
        medicines = newMedicines
        clearSelection()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine_multi_select, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = medicines.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.tvName.text = medicine.name
        holder.tvDescription.text = medicine.description ?: "Без описания"
/*        holder.checkBox.visibility = View.VISIBLE
        holder.checkBox.isChecked = selectedItems.contains(medicine.id)*/
        // Если флаг showEditButton = true, то кнопка видна, иначе скрываем
        holder.btnEdit.visibility = if (showEditButton) View.VISIBLE else View.GONE
        if (selectedItems.contains(medicine.id)) {
            // Прозрачный синий, например, с альфа 0x80 (50% прозрачность)
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#802196F3"))
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
    }
}
