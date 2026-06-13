package com.autobook.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.autobook.app.data.local.entity.ServiceReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ServiceReminder): Long

    @Update
    suspend fun updateReminder(reminder: ServiceReminder)

    /** Most recent active (isDone = 0) reminder for a vehicle. */
    @Query("SELECT * FROM service_reminder WHERE vehicleId = :vehicleId AND isDone = 0 ORDER BY id DESC LIMIT 1")
    fun getActiveReminderByVehicle(vehicleId: Int): Flow<ServiceReminder?>

    /** All active reminders across all vehicles — used by the background worker. */
    @Query("SELECT * FROM service_reminder WHERE isDone = 0")
    suspend fun getAllActiveReminders(): List<ServiceReminder>

    @Query("UPDATE service_reminder SET isDone = 1 WHERE id = :reminderId")
    suspend fun markReminderDone(reminderId: Int)

    /**
     * Fulfills all active reminders for a vehicle. Called when a new service is
     * recorded, so stale reminders stop triggering daily worker notifications.
     */
    @Query("UPDATE service_reminder SET isDone = 1 WHERE vehicleId = :vehicleId AND isDone = 0")
    suspend fun markAllRemindersDoneForVehicle(vehicleId: Int)
}
