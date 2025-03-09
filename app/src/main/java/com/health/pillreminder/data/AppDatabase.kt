package com.health.pillreminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.health.pillreminder.data.dao.HistoryDao
import com.health.pillreminder.data.dao.MedicineDao
import com.health.pillreminder.data.dao.ScheduleEntryDao
import com.health.pillreminder.data.entities.HistoryEntry
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.data.entities.ScheduleEntry

// Обновите номер версии здесь (например, с 1 на 2)
@Database(entities = [Medicine::class, ScheduleEntry::class, HistoryEntry::class], version = 10)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun scheduleEntryDao(): ScheduleEntryDao
    abstract fun historyDao(): HistoryDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun init(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pillreminder.db"
                )
                    // fallbackToDestructiveMigration используется, если нет готовой миграции.
                    .fallbackToDestructiveMigration()
                    .build()
            }
        }

        fun getInstance(): AppDatabase {
            return INSTANCE ?: throw IllegalStateException("Database not initialized")
        }
    }
}
