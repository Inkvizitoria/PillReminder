package com.health.pillreminder.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.health.pillreminder.ui.fragments.DeletedMedicinesFragment
import com.health.pillreminder.ui.fragments.DeletedSchedulesFragment

class TrashPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DeletedMedicinesFragment()
            1 -> DeletedSchedulesFragment()
            else -> throw IllegalStateException("Недопустимая позиция")
        }
    }
}
