package com.health.pillreminder.data.model

data class DayBlockData(
    val year: Int,
    val month: Int, // 0..11
    val day: Int,
    val dayLabel: String,
    val dayStart: Long,
    val entriesCount: Int
)