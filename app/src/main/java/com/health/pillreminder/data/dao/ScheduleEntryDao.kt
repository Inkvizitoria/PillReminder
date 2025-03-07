package com.health.pillreminder.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.pillreminder.data.entities.ScheduleEntry

@Dao
interface ScheduleEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scheduleEntry: ScheduleEntry): Long

    @Query("SELECT * FROM schedule_entries WHERE scheduledTime BETWEEN :start AND :end ORDER BY scheduledTime")
    suspend fun getEntriesForDay(start: Long, end: Long): List<ScheduleEntry>

    @Query("DELETE FROM schedule_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE schedule_entries SET isDeleted = 1 WHERE medicineId = :medicineId")
    suspend fun markSchedulesAsDeleted(medicineId: Long)

    @Query("SELECT * FROM schedule_entries WHERE id = :id LIMIT 1")
    suspend fun getScheduleEntryById(id: Long): ScheduleEntry?

    @Query("SELECT * FROM schedule_entries LEFT JOIN medicines medicine on medicineId = medicine.id")
    suspend fun getAllScheduleEntries(): List<ScheduleEntry>
}
