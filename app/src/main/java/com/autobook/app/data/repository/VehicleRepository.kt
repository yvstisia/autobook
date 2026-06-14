package com.autobook.app.data.repository

import com.autobook.app.data.local.dao.VehicleDao
import com.autobook.app.data.local.entity.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {

    fun getAllVehicles(): Flow<List<Vehicle>> = vehicleDao.getAllVehicles()

    suspend fun getVehicleById(id: Int): Vehicle? = vehicleDao.getVehicleById(id)

    suspend fun insertVehicle(vehicle: Vehicle): Long = vehicleDao.insertVehicle(vehicle)

    suspend fun updateVehicle(vehicle: Vehicle) = vehicleDao.updateVehicle(vehicle)

    suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.deleteVehicle(vehicle)

    /** Raises the vehicle's odometer to [odometer] when higher; keeps all screens in sync. */
    suspend fun bumpOdometerIfHigher(vehicleId: Int, odometer: Int) =
        vehicleDao.bumpOdometerIfHigher(vehicleId, odometer)
}
