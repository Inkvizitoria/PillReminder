package com.health.pillreminder.ui.fragments

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class HistoryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> HistoryIntakesFragment()
            1 -> HistorySchedulesFragment()
            else -> throw IllegalArgumentException("Invalid tab position")
        }
    }
}
