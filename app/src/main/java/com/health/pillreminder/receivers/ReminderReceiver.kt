package com.health.pillreminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.health.pillreminder.R
import com.health.pillreminder.util.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val scheduleEntryId = intent?.getLongExtra("scheduleEntryId", -1L) ?: -1L
        val reminderIndex = intent?.getIntExtra("reminderIndex", 0) ?: 0

        // Здесь можно добавить проверку, был ли подтвержден прием
        // Для демонстрации просто отправляем уведомление

        val notificationId = (scheduleEntryId * 10 + reminderIndex).toInt()
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)  // иконка должна быть в ресурсах
            .setContentTitle("Прием лекарства")
            .setContentText("Пора принять лекарство!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
