package com.autobook.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.autobook.app.data.local.entity.FuelLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelLog(log: FuelLog): Long

    @Update
    suspend fun updateFuelLog(log: FuelLog)

    @Delete
    suspend fun deleteFuelLog(log: FuelLog)

    @Query("SELECT * FROM fuel_log WHERE id = :id")
    suspend fun getFuelLogById(id: Int): FuelLog?

    @Query("SELECT * FROM fuel_log WHERE vehicleId = :vehicleId ORDER BY fillDate DESC")
    fun getFuelLogsByVehicle(vehicleId: Int): Flow<List<FuelLog>>

    /** All fuel logs for a vehicle in odometer order, used to recompute kmPerLiter after edits. */
    @Query("SELECT * FROM fuel_log WHERE vehicleId = :vehicleId ORDER BY odometerAtFill ASC, fillDate ASC")
    suspend fun getFuelLogsByVehicleOrdered(vehicleId: Int): List<FuelLog>

    /** The most recent fuel log by odometer, used to compute kmPerLiter for the next fill. */
    @Query("SELECT * FROM fuel_log WHERE vehicleId = :vehicleId ORDER BY odometerAtFill DESC, id DESC LIMIT 1")
    suspend fun getLastFuelLog(vehicleId: Int): FuelLog?

    /**
     * Sum of totalCost for fills within [startOfMonth, endOfMonth) for a vehicle.
     * Returns null when there are no rows.
     */
    @Query(
        "SELECT SUM(totalCost) FROM fuel_log " +
            "WHERE vehicleId = :vehicleId AND fillDate >= :startOfMonth AND fillDate < :endOfMonth"
    )
    fun getTotalFuelCostThisMonth(vehicleId: Int, startOfMonth: Long, endOfMonth: Long): Flow<Int?>

    /** Average km/L for fills within the month (ignores 0.0 first-entry values). */
    @Query(
        "SELECT AVG(kmPerLiter) FROM fuel_log " +
            "WHERE vehicleId = :vehicleId AND fillDate >= :startOfMonth AND fillDate < :endOfMonth AND kmPerLiter > 0"
    )
    fun getAverageKmPerLiterThisMonth(vehicleId: Int, startOfMonth: Long, endOfMonth: Long): Flow<Float?>
}
