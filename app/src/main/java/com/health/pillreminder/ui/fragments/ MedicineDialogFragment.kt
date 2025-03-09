package com.health.pillreminder.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.health.pillreminder.R
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.data.entities.Medicine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicineDialogFragment : DialogFragment() {

    // Callback, который вызовется после успешного добавления лекарства
    var onMedicineAdded: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_medicine, null)
        builder.setView(view)
            .setTitle("Добавить лекарство")
            .setPositiveButton("Добавить") { _, _ ->
                val etName = view.findViewById<EditText>(R.id.et_medicine_name)
                val etDescription = view.findViewById<EditText>(R.id.et_medicine_description)
                val name = etName.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (name.isEmpty()) {
                    ToastUtils.showCustomToast(requireContext(), "Название не может быть пустым", ToastType.ERROR)
                    return@setPositiveButton
                }

                // Сохраняем лекарство в базе данных
                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getInstance().medicineDao().insert(
                        Medicine(name = name, description = description)
                    )
                    withContext(Dispatchers.Main) {
                        ToastUtils.showCustomToast(requireContext().applicationContext, "Лекарство добавлено", ToastType.SUCCESS)
                        // Вызываем обратный вызов, чтобы родительский фрагмент обновил список
                        onMedicineAdded?.invoke()
                    }
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
        return builder.create()
    }
}
