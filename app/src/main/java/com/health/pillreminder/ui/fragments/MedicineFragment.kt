package com.health.pillreminder.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
    private lateinit var emptyMessage: TextView
    private lateinit var adapter: MedicineMultiSelectAdapter
    private var actionMode: ActionMode? = null

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
    ): View {
        val view = inflater.inflate(R.layout.fragment_medicine, container, false)
        recyclerView = view.findViewById(R.id.recycler_medicines)
        emptyMessage = view.findViewById(R.id.emptyMessage)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MedicineMultiSelectAdapter(emptyList()).apply {
            showEditButton = true
        }

        adapter.onItemClick = { medicine -> }
        adapter.onItemLongClick = {
            if (actionMode == null) {
                actionMode = (activity as? AppCompatActivity)?.startSupportActionMode(actionModeCallback)
            }
            updateActionModeTitle()
        }
        adapter.onEditClick = { medicine ->
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
        AppDatabase.getInstance().medicineDao().getAllMedicinesLiveData()
            .observe(viewLifecycleOwner) { medicines ->
                updateUI(medicines)
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
        lifecycleScope.launch(Dispatchers.IO) {
            val medicines: List<Medicine> = AppDatabase.getInstance().medicineDao().getAllMedicines()
            withContext(Dispatchers.Main) {
                updateUI(medicines)
            }
        }
    }

    private fun updateUI(medicines: List<Medicine>) {
        if (medicines.isEmpty()) {
            emptyMessage.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyMessage.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateData(medicines)
        }
    }

    private fun deleteSelectedMedicines() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) return

        AlertDialog.Builder(requireContext())
            .setTitle("Удалить выбранные лекарства")
            .setMessage("Вы действительно хотите удалить выбранные лекарства? Их графики исчезнут из расписания, но останутся в истории.")
            .setPositiveButton("Удалить") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance()
                    val medicineDao = db.medicineDao()
                    val scheduleDao = db.scheduleEntryDao()

                    for (medicine in selected) {
                        // Помечаем графики как удаленные
                        scheduleDao.markSchedulesAsDeleted(medicine.id)

                        // Помечаем лекарство как удаленное
                        val updatedMedicine = medicine.copy(isDeleted = true)
                        medicineDao.update(updatedMedicine)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Лекарства и их графики скрыты", Toast.LENGTH_SHORT).show()
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
