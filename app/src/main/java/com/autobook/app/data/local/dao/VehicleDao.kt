package com.autobook.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.autobook.app.data.local.entity.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)

    @Query("SELECT * FROM vehicle ORDER BY createdAt DESC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicle WHERE id = :id")
    suspend fun getVehicleById(id: Int): Vehicle?

    /**
     * Raises a vehicle's odometer to [odometer] if it is higher than the stored value, so the
     * latest service/fuel entry keeps the vehicle's current odometer in sync. Never lowers it.
     */
    @Query("UPDATE vehicle SET currentOdometer = :odometer WHERE id = :vehicleId AND currentOdometer < :odometer")
    suspend fun bumpOdometerIfHigher(vehicleId: Int, odometer: Int)
}
