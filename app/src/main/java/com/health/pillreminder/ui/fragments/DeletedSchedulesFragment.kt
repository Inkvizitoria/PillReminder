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
import com.health.pillreminder.data.entities.ScheduleEntry
import com.health.pillreminder.ui.adapters.ScheduleMultiSelectAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeletedSchedulesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleMultiSelectAdapter
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
                    restoreSelectedSchedules()
                    mode?.finish()
                    true
                }
                R.id.action_clear -> {
                    clearSelectedSchedules()
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
        val view = inflater.inflate(R.layout.fragment_trash_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerTrashList)
        emptyMessage = view.findViewById(R.id.emptyMessage)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ScheduleMultiSelectAdapter(emptyList()) // Исправленный вызов конструктора
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
        loadDeletedSchedules()
    }

    private fun loadDeletedSchedules() {
        CoroutineScope(Dispatchers.IO).launch {
            val deletedSchedules: List<ScheduleEntry> = AppDatabase.getInstance().scheduleEntryDao().getAllDeletedScheduleEntries()
            withContext(Dispatchers.Main) {
                adapter.updateData(deletedSchedules)
                emptyMessage.visibility = if (deletedSchedules.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility = if (deletedSchedules.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun restoreSelectedSchedules() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            for (schedule in selected) {
                AppDatabase.getInstance().scheduleEntryDao().restoreSchedule(schedule.id)
            }
            withContext(Dispatchers.Main) {
                ToastUtils.showCustomToast(requireContext(), "Графики восстановлены", ToastType.SUCCESS)
                loadDeletedSchedules()
            }
        }
    }

    private fun clearSelectedSchedules() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) return
        AlertDialog.Builder(requireContext())
            .setTitle("Окончательно удалить выбранные графики")
            .setMessage("Вы уверены, что хотите удалить выбранные графики без возможности восстановления?")
            .setPositiveButton("Удалить") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    for (schedule in selected) {
                        AppDatabase.getInstance().scheduleEntryDao().deleteById(schedule.id)
                    }
                    withContext(Dispatchers.Main) {
                        ToastUtils.showCustomToast(requireContext(), "Графики удалены окончательно", ToastType.ERROR)
                        loadDeletedSchedules()
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
