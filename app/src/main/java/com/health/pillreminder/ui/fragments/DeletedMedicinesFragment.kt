package com.health.pillreminder.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.ui.adapters.MedicineMultiSelectAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeletedMedicinesFragment : Fragment(R.layout.fragment_trash_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicineMultiSelectAdapter
    private lateinit var emptyMessage: TextView
    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.trash_context_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_restore -> {
                    restoreSelectedMedicines()
                    mode?.finish()
                    true
                }
                R.id.action_clear -> {
                    clearSelectedMedicines()
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

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
        adapter.clearSelection()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_trash_history, container, false)
        recyclerView = view.findViewById(R.id.recycler_history)
        emptyMessage = view.findViewById(R.id.emptyMessage)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MedicineMultiSelectAdapter(emptyList()).apply {
            showEditButton = false
        }
        adapter.onItemLongClick = {
            if (actionMode == null) {
                actionMode = (activity as? AppCompatActivity)?.startSupportActionMode(actionModeCallback)
            }
            updateActionModeTitle()
        }
        recyclerView.adapter = adapter
        return view
    }

    override fun onResume() {
        super.onResume()
        loadDeletedMedicines()
    }

    private fun loadDeletedMedicines() {
        CoroutineScope(Dispatchers.IO).launch {
            val deleted: List<Medicine> = AppDatabase.getInstance().medicineDao().getDeletedMedicines()
            withContext(Dispatchers.Main) {
                adapter.updateData(deleted)
                emptyMessage.visibility = if (deleted.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility = if (deleted.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun restoreSelectedMedicines() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            for (medicine in selected) {
                val restored = medicine.copy(isDeleted = false)
                AppDatabase.getInstance().medicineDao().update(restored)
            }
            withContext(Dispatchers.Main) {
                ToastUtils.showCustomToast(requireContext(), "Лекарства восстановлены", ToastType.SUCCESS)
                loadDeletedMedicines()
            }
        }
    }

    private fun clearSelectedMedicines() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) return
        AlertDialog.Builder(requireContext())
            .setTitle("Окончательно удалить выбранные лекарства")
            .setMessage("Вы уверены, что хотите удалить выбранные лекарства без возможности восстановления?")
            .setPositiveButton("Удалить") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    for (medicine in selected) {
                        AppDatabase.getInstance().medicineDao().delete(medicine)
                        AppDatabase.getInstance().scheduleEntryDao().deleteByMedicineId(medicine.id)
                    }
                    withContext(Dispatchers.Main) {
                        ToastUtils.showCustomToast(requireContext(), "Лекарства удалены окончательно", ToastType.ERROR)
                        loadDeletedMedicines()
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
