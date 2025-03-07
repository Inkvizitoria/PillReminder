package com.health.pillreminder.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduleEntryId: Long?,   // связь с графиком (ScheduleEntry)
    val intakeTime: Long,         // конкретное время приёма (timestamp)
    val date: Long,               // время создания записи (обычно текущее время)
    val status: HistoryStatus,
    val comment: String? = null
)
