package com.health.pillreminder.data.model


data class HistoryScheduleDay(
    val dateMillis: Long,
    val items: List<HistoryScheduleItem>
)