package com.autobook.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autobook.app.data.local.entity.ServiceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceRecord(record: ServiceRecord): Long

    @Delete
    suspend fun deleteServiceRecord(record: ServiceRecord)

    @Query("SELECT * FROM service_record WHERE vehicleId = :vehicleId ORDER BY serviceDate DESC")
    fun getServiceRecordsByVehicle(vehicleId: Int): Flow<List<ServiceRecord>>

    @Query("SELECT * FROM service_record WHERE vehicleId = :vehicleId ORDER BY serviceDate DESC, id DESC LIMIT 1")
    suspend fun getLatestServiceRecord(vehicleId: Int): ServiceRecord?
}
