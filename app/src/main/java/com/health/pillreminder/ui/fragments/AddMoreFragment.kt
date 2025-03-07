package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.health.pillreminder.R

class AddMoreFragment : Fragment() {

    interface AddMoreListener {
        fun onAddMoreResult(addMore: Boolean)
    }

    var addMoreListener: AddMoreListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Загружаем макет, который содержит текст и кнопки "Да" и "Нет"
        return inflater.inflate(R.layout.fragment_add_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnYes = view.findViewById<Button>(R.id.btnYes)
        val btnNo = view.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            addMoreListener?.onAddMoreResult(true)
        }
        btnNo.setOnClickListener {
            addMoreListener?.onAddMoreResult(false)
        }
    }
}
