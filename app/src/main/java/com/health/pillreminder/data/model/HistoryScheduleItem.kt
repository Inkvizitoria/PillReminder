package com.health.pillreminder.data.model

data class HistoryScheduleItem(
    val scheduleId: Long,
    val activeStartTime: Long,
    val activeEndTime: Long,
    val medicineName: String,
    val dosage: String,       // например, "2 таблетки" или "10 мг"
    val period: String,       // описание периода графика, например, "с 01.03.2025 по 10.03.2025" или "каждые 4 часа"
    val scheduleTime: String, // текстовое представление времени графика
    val historyDate: Long     // дата для группировки (например, periodEnd)
)
