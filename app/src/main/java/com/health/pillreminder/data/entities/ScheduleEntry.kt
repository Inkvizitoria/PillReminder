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
    val dosageUnit: String,         // "таблетка" или "мг"
    val scheduledTime: Long,        // если фиксированное время, хранится как timestamp
    val repeatValue: Int?,          // если повторяемое, число (например, каждые 2)
    val repeatUnit: String?,        // "минут", "часов", "дней"
    val periodStart: Long,          // начало периода графика
    val periodEnd: Long,             // конец периода графика
    val reminderConfig: ReminderConfig? = null
)
