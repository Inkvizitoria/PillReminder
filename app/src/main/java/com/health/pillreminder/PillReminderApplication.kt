package com.health.pillreminder

import android.app.Application
import com.health.pillreminder.data.AppDatabase
import com.health.pillreminder.util.NotificationHelper

class PillReminderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализация базы данных
        AppDatabase.init(this)
        // Создание канала для уведомлений
        NotificationHelper.createNotificationChannel(this)
    }
}
