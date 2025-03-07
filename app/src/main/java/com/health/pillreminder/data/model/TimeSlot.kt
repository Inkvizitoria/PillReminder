package com.health.pillreminder.data.model

import com.health.pillreminder.data.entities.ScheduleEntry

data class TimeSlot(
    val timestamp: Long,
    val entries: List<ScheduleEntry>
)