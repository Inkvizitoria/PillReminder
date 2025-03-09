package com.health.pillreminder.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.health.pillreminder.R

class ColorPickerDialogFragment : DialogFragment() {

    interface ColorPickerListener {
        fun onColorPicked(colorKey: String)
        fun onColorPickCancelled()
    }

    var listener: ColorPickerListener? = null

    // Расширенный список ключей
    private val colorKeys = listOf(
        "red_light", "red", "red_dark", "red_extra",
        "orange_light", "orange", "orange_dark", "orange_extra",
        "yellow_light", "yellow", "yellow_dark", "yellow_extra",
        "green_light", "green", "green_dark", "green_extra",
        "blue_light", "blue", "blue_dark", "blue_extra",
        "indigo_light", "indigo", "indigo_dark", "indigo_extra",
        "violet_light", "violet", "violet_dark", "violet_extra",
        "pink_light", "pink", "pink_dark",
        "cyan", "cyan_dark", "lime", "lime_dark",
        "brown_light"
    )

    private var selectedView: ImageView? = null
    private var selectedViewNormalResId: Int = 0
    private var selectedColorKey: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = android.app.AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_color_picker, null)

        val gridLayout = view.findViewById<GridLayout>(R.id.gridColors)
        val btnOk = view.findViewById<MaterialButton>(R.id.btnOk)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        // Устанавливаем количество столбцов в GridLayout
        gridLayout.columnCount = 4

        // Для каждого ключа создаём ImageView с нужными параметрами
        for (colorKey in colorKeys) {
            val imageView = ImageView(requireContext())
            // Размер элемента (например, 60dp)
            val size = resources.getDimensionPixelSize(R.dimen.color_picker_item_size)
            val layoutParams = GridLayout.LayoutParams().apply {
                width = size
                height = size
                setMargins(8, 8, 8, 8)
            }
            imageView.layoutParams = layoutParams

            // Формируем имена ресурсов:
            // нормальное состояние: "circle_{colorKey}"
            // выбранное состояние: "layer_circle_{colorKey}_selected"
            val normalResName = "circle_$colorKey"
            val selectedResName = "layer_circle_${colorKey}_selected"
            val normalResId = resources.getIdentifier(normalResName, "drawable", requireContext().packageName)
            val selectedResId = resources.getIdentifier(selectedResName, "drawable", requireContext().packageName)

            // Устанавливаем нормальное изображение по умолчанию
            if (normalResId != 0) {
                imageView.setImageResource(normalResId)
            } else {
                imageView.setBackgroundColor(android.graphics.Color.LTGRAY)
            }

            // Устанавливаем обработчик клика
            imageView.setOnClickListener {
                // Сбрасываем изображение у предыдущего выбранного элемента
                selectedView?.setImageResource(selectedViewNormalResId)
                // Устанавливаем выбранное изображение для текущего элемента

                if (selectedResId != 0) {
                    imageView.setImageResource(selectedResId)
                }
                selectedView = imageView
                selectedColorKey = colorKey
                Log.d("TEST", selectedColorKey.toString())
                selectedViewNormalResId = normalResId
            }
            gridLayout.addView(imageView)
        }

        btnOk.setOnClickListener {
            if (selectedColorKey != null) {
                listener?.onColorPicked(selectedColorKey!!)
                Log.d("TEST", selectedColorKey!!.toString())

            } else {
                listener?.onColorPickCancelled()
            }
            dismiss()
        }
        btnCancel.setOnClickListener {
            listener?.onColorPickCancelled()
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }
}
