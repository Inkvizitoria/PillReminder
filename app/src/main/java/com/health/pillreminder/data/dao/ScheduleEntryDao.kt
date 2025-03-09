package com.health.pillreminder.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.health.pillreminder.data.entities.Medicine
import com.health.pillreminder.data.entities.ScheduleEntry

@Dao
interface ScheduleEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scheduleEntry: ScheduleEntry): Long

    @Update
    suspend fun update(schedule: ScheduleEntry)

    @Query("SELECT * FROM schedule_entries WHERE scheduledTime BETWEEN :start AND :end ORDER BY scheduledTime")
    suspend fun getEntriesForDay(start: Long, end: Long): List<ScheduleEntry>

    @Query("UPDATE schedule_entries SET isDeleted = 0 WHERE id = :id")
    suspend fun updateById(id: Long)
    @Query("DELETE FROM schedule_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
    @Query("DELETE FROM schedule_entries WHERE id = :medicineId")
    suspend fun deleteByMedicineId(medicineId: Long)

    @Query("UPDATE schedule_entries SET isDeleted = 1 WHERE id = :id")
    suspend fun markScheduleAsDeleted(id: Long)

    @Query("UPDATE schedule_entries SET isDeleted = 1 WHERE medicineId = :medicineId")
    suspend fun markSchedulesAsDeleted(medicineId: Long)

    @Query("UPDATE schedule_entries SET isDeleted = 0 WHERE id = :id")
    suspend fun restoreSchedule(id: Long)

    @Query("SELECT * FROM schedule_entries WHERE id = :id LIMIT 1")
    suspend fun getScheduleEntryById(id: Long): ScheduleEntry?

    @Query("SELECT s.*, medicine.name, medicine.description FROM schedule_entries s LEFT JOIN medicines medicine on medicineId = medicine.id ORDER BY s.id DESC")
    suspend fun getAllScheduleEntries(): List<ScheduleEntry>

    @Query("SELECT * FROM schedule_entries WHERE isDeleted = 1 ORDER BY periodEnd DESC")
    suspend fun getAllDeletedScheduleEntries(): List<ScheduleEntry>
}
