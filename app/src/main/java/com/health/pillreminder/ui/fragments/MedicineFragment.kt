package com.health.pillreminder.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.ui.adapters.MedicineMultiSelectAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicineFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicineMultiSelectAdapter
    private var actionMode: ActionMode? = null

    // Action Mode callback для массовых действий (например, удаление)
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.medicine_context_menu, menu)
            return true
        }
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_delete -> {
                    deleteSelectedMedicines()
                    mode?.finish()
                    true
                }
                else -> false
            }
        }
        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.clearSelection()
            actionMode = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine, container, false)
        recyclerView = view.findViewById(R.id.recycler_medicines)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MedicineMultiSelectAdapter(emptyList()).apply {
            showEditButton = true
        }

        adapter.onItemClick = { medicine ->
            // Обычный клик, если не выбран режим мультивыбора
            // Можно открыть диалог редактирования или выполнить другое действие
        }
        adapter.onItemLongClick = {
            if (actionMode == null) {
                actionMode = (activity as? AppCompatActivity)?.startSupportActionMode(actionModeCallback)
            }
            updateActionModeTitle()
        }
        // Устанавливаем callback для кнопки редактирования:
        adapter.onEditClick = { medicine ->
            // Здесь вызываем именно нужную модалку:
            EditMedicineDialogFragment.newInstance(medicine)
                .show(childFragmentManager, "EditMedicineDialog")
        }
        recyclerView.adapter = adapter

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_add_medicine)
        fab.setOnClickListener {
            val dialog = MedicineDialogFragment()
            dialog.onMedicineAdded = { loadMedicines() }
            dialog.show(childFragmentManager, "MedicineDialog")
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Подписываемся на LiveData из Room
        AppDatabase.getInstance().medicineDao().getAllMedicinesLiveData()
            .observe(viewLifecycleOwner) { medicines ->
                adapter.updateData(medicines)
            }
    }

    override fun onResume() {
        super.onResume()
        loadMedicines()
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
        adapter.clearSelection()
    }

    private fun loadMedicines() {
        CoroutineScope(Dispatchers.IO).launch {
            val medicines: List<Medicine> = AppDatabase.getInstance().medicineDao().getAllMedicines()
            withContext(Dispatchers.Main) {
                adapter.updateData(medicines)
            }
        }
    }

    private fun deleteSelectedMedicines() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) return
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить выбранные лекарства")
            .setMessage("Вы действительно хотите удалить выбранные лекарства?")
            .setPositiveButton("Удалить") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    for (medicine in selected) {
                        val updated = medicine.copy(isDeleted = true)
                        AppDatabase.getInstance().medicineDao().update(updated)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Лекарства удалены", Toast.LENGTH_SHORT).show()
                        loadMedicines()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateActionModeTitle() {
        val count = adapter.getSelectedItems().size
        actionMode?.title = "$count выбрано"
    }
}
