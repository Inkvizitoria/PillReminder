package com.health.pillreminder.data.model

import com.health.pillreminder.data.entities.ScheduleEntry

data class DayCell(
    val year: Int,
    val month: Int,
    val day: Int,
    val dayOfWeek: String,
    val entries: List<ScheduleEntry>, // список записей, которые попадают в этот день
    val entriesCount: Int
)