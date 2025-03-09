package com.health.pillreminder.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.health.pillreminder.R
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.ui.fragments.DayViewFragment
import com.health.pillreminder.ui.fragments.HistoryFragment
import com.health.pillreminder.ui.fragments.MedicineFragment
import com.health.pillreminder.ui.fragments.TrashFragment
import com.health.pillreminder.ui.fragments.MedicineSelectionForScheduleFragment
import com.health.pillreminder.ui.fragments.MonthCalendarGridFragment
import com.health.pillreminder.ui.fragments.ScheduleCreationFragment
import com.health.pillreminder.ui.fragments.ScheduleWizardFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Настраиваем Toolbar как appbar и устанавливаем название приложения
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_day_view -> loadFragment(MonthCalendarGridFragment())
                R.id.menu_medicine -> loadFragment(MedicineFragment())
                R.id.menu_schedule -> {
                    // Загружаем экран выбора лекарств
                    val selectionFragment = MedicineSelectionForScheduleFragment()
                    selectionFragment.listener = object : MedicineSelectionForScheduleFragment.OnMedicinesSelectedListener {
                        override fun onMedicinesSelectedForSchedule(medicines: List<Medicine>) {
                            // Когда лекарства выбраны, переходим к ScheduleCreationFragment
                            val creationFragment = ScheduleCreationFragment.newInstance(medicines)
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, creationFragment)
                                .commit()
                        }
                    }
                    loadFragment(selectionFragment)
                }
                R.id.menu_history -> loadFragment(HistoryFragment())  // Можно заменить, если нужен другой фрагмент
                R.id.menu_trash -> loadFragment(TrashFragment())
            }
            true
        }

        // Загружаем фрагмент по умолчанию
        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.menu_day_view
        }
    }
}
