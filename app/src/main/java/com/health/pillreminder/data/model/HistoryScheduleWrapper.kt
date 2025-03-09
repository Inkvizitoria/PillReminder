package com.health.pillreminder.data.model
data class HistoryScheduleWrapper(
    val day: HistoryScheduleDay,
    val scheduleItem: HistoryScheduleItem
) : java.io.Serializable
