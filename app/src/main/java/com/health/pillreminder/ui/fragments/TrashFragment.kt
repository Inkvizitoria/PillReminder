package com.health.pillreminder.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.health.pillreminder.R
import com.health.pillreminder.ui.adapters.TrashPagerAdapter

class TrashFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var emptyMessage: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_trash_tabs, container, false)
        tabLayout = view.findViewById(R.id.tabLayoutTrash)
        viewPager = view.findViewById(R.id.viewPagerTrash)
        //emptyMessage = view.findViewById(R.id.emptyMessage)

        val pagerAdapter = TrashPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Лекарства"
                1 -> tab.text = "Графики"
            }
        }.attach()

        return view
    }
}
