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
    val dosageUnit: String,         // Например, "Таблетка" или "мг"
    val scheduledTime: Long,        // Если фиксированное время, timestamp
    val repeatValue: Int?,          // Если повторяющееся, например, каждые 2
    val repeatUnit: String?,        // "Минут", "Часов", "Дней"
    val periodStart: Long,          // Начало периода графика (например, дата начала расписания)
    val periodEnd: Long,            // Конец периода графика
    val reminderConfig: ReminderConfig? = null,
    val blockColor: String = "#5BAF4C",  // Цвет блока (HEX-значение)
    val isDeleted: Boolean = false,      // Флаг удаления
    // Новые поля: время начала и окончания активного периода (относительно полуночи)
    val activeStartTime: Long = 0L,  // Например, для 10:00: 10 * 60 * 60 * 1000 = 36000000
    val activeEndTime: Long = 0L,    // Например, для 19:00: 19 * 60 * 60 * 1000 = 68400000
    val deletionTimestamp: Long? = null // 🆕 Время удаления (для корзины)
)
