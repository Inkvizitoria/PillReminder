package com.health.pillreminder.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.health.pillreminder.R
import androidx.viewpager2.widget.ViewPager2

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabLayout = view.findViewById(R.id.tabLayoutHistory)
        viewPager = view.findViewById(R.id.viewPagerHistory)

        val adapter = HistoryPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "История приёмов"
                1 -> tab.text = "История графиков"
            }
        }.attach()
    }
}
