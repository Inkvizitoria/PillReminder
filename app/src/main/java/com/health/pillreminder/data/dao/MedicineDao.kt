package com.health.pillreminder.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.health.pillreminder.data.entities.Medicine

@Dao
interface MedicineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: Medicine): Long

    // Выборка активных (не удалённых) лекарств
    @Query("SELECT * FROM medicines WHERE isDeleted = 0")
    suspend fun getAllMedicines(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    suspend fun getMedicineById(id: Long): Medicine?

    // Выборка удалённых (для истории)
    @Query("SELECT * FROM medicines WHERE isDeleted = 1")
    suspend fun getDeletedMedicines(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE isDeleted = 0")
    fun getAllMedicinesLiveData(): LiveData<List<Medicine>>

    @Update
    suspend fun update(medicine: Medicine)

    @Delete
    suspend fun delete(medicine: Medicine)
}
