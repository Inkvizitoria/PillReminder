package com.health.pillreminder.data.model

data class ReminderConfig(
    val advanceMinutes: Int = 10,              // Напоминание за 10 минут до
    val repeatTimes: List<Int> = listOf(0, 10),  // Повтор: в момент приема и через 10 минут после
    val repeatValue: Int? = null,              // Интервал повторения (например, 2)
    val repeatUnit: String? = null             // Единица измерения ("Минут", "Часов", "Дней")
)
