package com.health.pillreminder.data.model


data class HistoryIntakeItem(
    val historyId: Long,      // ID записи истории (HistoryEntry.id)
    val medicineName: String,
    val intakeTime: Long,
    val plannedDate: Long,    // запланированное время приёма (из schedule_entry либо intakeTime)
    val status: String,       // человекочитаемое значение ("Принято" / "Пропущено")
    val dosage: String,       // например, "2 таблетки"
    val historyDate: Long     // дата создания записи истории
)


