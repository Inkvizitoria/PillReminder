package com.health.pillreminder.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.health.pillreminder.data.model.ReminderConfig

@Entity(tableName = "schedule_entries")
data class ScheduleEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val name: String,
    val dosage: Float,
    val dosageUnit: String,
    val scheduledTime: Long,
    val repeatValue: Int?,
    val repeatUnit: String?,
    val periodStart: Long,
    val periodEnd: Long,
    val reminderConfig: ReminderConfig? = null,
    val isDeleted: Boolean = false // <-- Новый флаг
)

