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

    /** All active (isDone = 0) reminders for a vehicle — one per service type. */
    @Query("SELECT * FROM service_reminder WHERE vehicleId = :vehicleId AND isDone = 0 ORDER BY id DESC")
    fun getActiveRemindersByVehicle(vehicleId: Int): Flow<List<ServiceReminder>>

    /** All active reminders across all vehicles — used by the background worker. */
    @Query("SELECT * FROM service_reminder WHERE isDone = 0")
    suspend fun getAllActiveReminders(): List<ServiceReminder>

    /** Reactive variant of [getAllActiveReminders] for the in-app notifications inbox. */
    @Query("SELECT * FROM service_reminder WHERE isDone = 0")
    fun getAllActiveRemindersFlow(): Flow<List<ServiceReminder>>

    @Query("UPDATE service_reminder SET isDone = 1 WHERE id = :reminderId")
    suspend fun markReminderDone(reminderId: Int)

    /** Removes all reminders attached to a record, used when regenerating after an edit. */
    @Query("DELETE FROM service_reminder WHERE serviceRecordId = :serviceRecordId")
    suspend fun deleteRemindersForRecord(serviceRecordId: Int)

    /**
     * Fulfills active reminders for a vehicle that match a specific service type. Called when
     * a service of that type is logged, so the old reminder is replaced by the new one — while
     * reminders for *other* types (and unrelated logs like a tire change) stay untouched.
     */
    @Query("UPDATE service_reminder SET isDone = 1 WHERE vehicleId = :vehicleId AND serviceType = :serviceType AND isDone = 0")
    suspend fun markRemindersDoneForType(vehicleId: Int, serviceType: String)
}
