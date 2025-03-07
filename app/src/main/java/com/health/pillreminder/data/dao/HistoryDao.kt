package com.health.pillreminder.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.pillreminder.data.entities.HistoryEntry

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(historyEntry: HistoryEntry): Long

    @Query("SELECT * FROM history_entries WHERE scheduleEntryId IN (:ids)")
    suspend fun getHistoryForScheduleEntries(ids: List<Long>): List<HistoryEntry>

    @Query("SELECT * FROM history_entries ORDER BY date DESC")
    suspend fun getAllHistory(): List<HistoryEntry>

    @Query("DELETE FROM history_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
