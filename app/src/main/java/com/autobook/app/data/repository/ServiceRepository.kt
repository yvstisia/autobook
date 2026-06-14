package com.autobook.app.data.repository

import com.autobook.app.data.local.dao.ServiceRecordDao
import com.autobook.app.data.local.dao.ServiceReminderDao
import com.autobook.app.data.local.entity.ServiceRecord
import com.autobook.app.data.local.entity.ServiceReminder
import kotlinx.coroutines.flow.Flow

class ServiceRepository(
    private val serviceRecordDao: ServiceRecordDao,
    private val serviceReminderDao: ServiceReminderDao
) {

    // --- ServiceRecord ---
    fun getServiceRecordsByVehicle(vehicleId: Int): Flow<List<ServiceRecord>> =
        serviceRecordDao.getServiceRecordsByVehicle(vehicleId)

    suspend fun insertServiceRecord(record: ServiceRecord): Long =
        serviceRecordDao.insertServiceRecord(record)

    suspend fun updateServiceRecord(record: ServiceRecord) =
        serviceRecordDao.updateServiceRecord(record)

    suspend fun deleteServiceRecord(record: ServiceRecord) =
        serviceRecordDao.deleteServiceRecord(record)

    suspend fun getServiceRecordById(id: Int): ServiceRecord? =
        serviceRecordDao.getServiceRecordById(id)

    suspend fun getLatestServiceRecord(vehicleId: Int): ServiceRecord? =
        serviceRecordDao.getLatestServiceRecord(vehicleId)

    // --- ServiceReminder ---
    suspend fun insertReminder(reminder: ServiceReminder): Long =
        serviceReminderDao.insertReminder(reminder)

    suspend fun markReminderDone(reminderId: Int) =
        serviceReminderDao.markReminderDone(reminderId)

    suspend fun markRemindersDoneForType(vehicleId: Int, serviceType: String) =
        serviceReminderDao.markRemindersDoneForType(vehicleId, serviceType)

    suspend fun deleteRemindersForRecord(serviceRecordId: Int) =
        serviceReminderDao.deleteRemindersForRecord(serviceRecordId)

    fun getActiveRemindersByVehicle(vehicleId: Int): Flow<List<ServiceReminder>> =
        serviceReminderDao.getActiveRemindersByVehicle(vehicleId)

    suspend fun getAllActiveReminders(): List<ServiceReminder> =
        serviceReminderDao.getAllActiveReminders()

    fun getAllActiveRemindersFlow(): Flow<List<ServiceReminder>> =
        serviceReminderDao.getAllActiveRemindersFlow()
}
