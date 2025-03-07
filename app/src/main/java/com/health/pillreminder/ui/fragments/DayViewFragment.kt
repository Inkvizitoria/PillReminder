package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.health.pillreminder.R

class DayViewFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Здесь можно реализовать кастомный RecyclerView, отображающий каждый час дня и карточки с записями приёма
        return inflater.inflate(R.layout.fragment_day_view, container, false)
    }
}
