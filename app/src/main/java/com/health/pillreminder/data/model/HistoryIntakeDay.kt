package com.health.pillreminder.data.model


data class HistoryIntakeDay(
    val dateMillis: Long,
    val items: List<HistoryIntakeItem>
)