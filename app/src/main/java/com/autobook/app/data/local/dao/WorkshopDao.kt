package com.autobook.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.autobook.app.data.local.entity.Workshop
import kotlinx.coroutines.flow.Flow

/** Visit count for a workshop, keyed by its normalized (lower-cased, trimmed) name. */
data class WorkshopVisitCount(val name: String, val visits: Int)

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

    /** Case-insensitive name lookup, used to avoid duplicate workshops when syncing from a service. */
    @Query("SELECT * FROM workshop WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun findByName(name: String): Workshop?

    /** How many service records reference each workshop, keyed by normalized name. */
    @Query(
        "SELECT LOWER(TRIM(workshopName)) AS name, COUNT(*) AS visits FROM service_record " +
            "WHERE workshopName IS NOT NULL AND TRIM(workshopName) != '' " +
            "GROUP BY LOWER(TRIM(workshopName))"
    )
    fun getWorkshopVisitCounts(): Flow<List<WorkshopVisitCount>>
}
