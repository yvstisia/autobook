package com.autobook.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.autobook.app.data.local.entity.Workshop
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkshopDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkshop(workshop: Workshop): Long

    @Update
    suspend fun updateWorkshop(workshop: Workshop)

    @Delete
    suspend fun deleteWorkshop(workshop: Workshop)

    @Query("SELECT * FROM workshop ORDER BY savedAt DESC")
    fun getAllWorkshops(): Flow<List<Workshop>>

    @Query("SELECT * FROM workshop WHERE id = :id")
    suspend fun getWorkshopById(id: Int): Workshop?
}
