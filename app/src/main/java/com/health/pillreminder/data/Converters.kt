package com.health.pillreminder.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.health.pillreminder.data.model.ReminderConfig

class Converters {
    @TypeConverter
    fun fromReminderConfig(reminderConfig: ReminderConfig?): String? {
        return Gson().toJson(reminderConfig)
    }

    @TypeConverter
    fun toReminderConfig(value: String?): ReminderConfig? {
        if (value == null) return null
        val type = object : TypeToken<ReminderConfig>() {}.type
        return Gson().fromJson(value, type)
    }
}
