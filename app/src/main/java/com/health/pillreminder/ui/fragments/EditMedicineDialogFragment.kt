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

class EditMedicineDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_MEDICINE_ID = "arg_medicine_id"
        private const val ARG_MEDICINE_NAME = "arg_medicine_name"
        private const val ARG_MEDICINE_DESCRIPTION = "arg_medicine_description"

        fun newInstance(medicine: Medicine): EditMedicineDialogFragment {
            val fragment = EditMedicineDialogFragment()
            val args = Bundle()
            args.putLong(ARG_MEDICINE_ID, medicine.id)
            args.putString(ARG_MEDICINE_NAME, medicine.name)
            args.putString(ARG_MEDICINE_DESCRIPTION, medicine.description)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_edit_medicine, null)
        val etName = view.findViewById<EditText>(R.id.et_medicine_name)
        val etDescription = view.findViewById<EditText>(R.id.et_medicine_description)

        // Предзаполнение полей из аргументов
        val medicineId = arguments?.getLong(ARG_MEDICINE_ID) ?: 0L
        val name = arguments?.getString(ARG_MEDICINE_NAME) ?: ""
        val description = arguments?.getString(ARG_MEDICINE_DESCRIPTION) ?: ""
        etName.setText(name)
        etDescription.setText(description)

        builder.setView(view)
            .setTitle("Редактировать лекарство")
            .setPositiveButton("Сохранить") { dialog, _ ->
                val updatedName = etName.text.toString().trim()
                val updatedDescription = etDescription.text.toString().trim()
                if (updatedName.isEmpty()) {
                    ToastUtils.showCustomToast(requireContext(), "Название не может быть пустым", ToastType.ERROR)
                    return@setPositiveButton
                }
                val updatedMedicine = Medicine(
                    id = medicineId,
                    name = updatedName,
                    description = updatedDescription,
                    isDeleted = false
                )
                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getInstance().medicineDao().update(updatedMedicine)
                    withContext(Dispatchers.Main) {
                        ToastUtils.showCustomToast(requireContext(), "Лекарство обновлено", ToastType.SUCCESS)
                    }
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
        return builder.create()
    }
}
