package com.health.pillreminder.ui.fragments

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SpecificIntakePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PeriodDateFragment()  // Фрагмент для выбора периода с датами
            1 -> SpecificDateFragment() // Фрагмент для выбора конкретной даты
            else -> Fragment()
        }
    }
}
