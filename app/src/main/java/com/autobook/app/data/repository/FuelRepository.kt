package com.autobook.app.data.repository

import com.autobook.app.data.local.dao.FuelLogDao
import com.autobook.app.data.local.entity.FuelLog
import com.autobook.app.util.monthRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

class FuelRepository(private val fuelLogDao: FuelLogDao) {

    fun getFuelLogsByVehicle(vehicleId: Int): Flow<List<FuelLog>> =
        fuelLogDao.getFuelLogsByVehicle(vehicleId)

    suspend fun getLastFuelLog(vehicleId: Int): FuelLog? =
        fuelLogDao.getLastFuelLog(vehicleId)

    suspend fun getFuelLogById(id: Int): FuelLog? =
        fuelLogDao.getFuelLogById(id)

    /** Deletes a log and recomputes km/L for the vehicle (the next fill depended on it). */
    suspend fun deleteFuelLog(log: FuelLog) {
        fuelLogDao.deleteFuelLog(log)
        recalculateKmPerLiter(log.vehicleId)
    }

    /**
     * Updates a fuel log's editable fields, recomputes totalCost, then recomputes km/L for
     * the whole vehicle so this entry and the next dependent entry stay correct.
     */
    suspend fun updateFuelLog(
        id: Int,
        vehicleId: Int,
        fillDate: Long,
        liters: Float,
        pricePerLiter: Int,
        odometerAtFill: Int,
        fuelType: String
    ) {
        val totalCost = (liters * pricePerLiter).roundToInt()
        fuelLogDao.updateFuelLog(
            FuelLog(
                id = id,
                vehicleId = vehicleId,
                fillDate = fillDate,
                liters = liters,
                pricePerLiter = pricePerLiter,
                totalCost = totalCost,
                odometerAtFill = odometerAtFill,
                fuelType = fuelType,
                kmPerLiter = 0.0f // set by recalculateKmPerLiter below
            )
        )
        recalculateKmPerLiter(vehicleId)
    }

    /**
     * Recomputes kmPerLiter for every fill of a vehicle, in odometer order. Each entry's
     * value = (its odometer - previous entry's odometer) / its liters; the first is 0.
     */
    private suspend fun recalculateKmPerLiter(vehicleId: Int) {
        val ordered = fuelLogDao.getFuelLogsByVehicleOrdered(vehicleId)
        var previousOdometer: Int? = null
        for (log in ordered) {
            val prev = previousOdometer
            val kmpl = if (prev != null && log.liters > 0f) {
                val distance = log.odometerAtFill - prev
                if (distance > 0) distance / log.liters else 0.0f
            } else {
                0.0f
            }
            if (kmpl != log.kmPerLiter) {
                fuelLogDao.updateFuelLog(log.copy(kmPerLiter = kmpl))
            }
            previousOdometer = log.odometerAtFill
        }
    }

    /**
     * Inserts a fuel log, auto-calculating [FuelLog.totalCost] and [FuelLog.kmPerLiter].
     * kmPerLiter = (odometerAtFill - previous odometerAtFill) / liters, or 0.0 when there
     * is no previous log or the distance is non-positive (e.g. odometer not advanced).
     */
    suspend fun insertFuelLog(
        vehicleId: Int,
        fillDate: Long,
        liters: Float,
        pricePerLiter: Int,
        odometerAtFill: Int,
        fuelType: String
    ): Long {
        val totalCost = (liters * pricePerLiter).roundToInt()

        val last = fuelLogDao.getLastFuelLog(vehicleId)
        val kmPerLiter = if (last != null && liters > 0f) {
            val distance = odometerAtFill - last.odometerAtFill
            if (distance > 0) distance / liters else 0.0f
        } else {
            0.0f
        }

        val log = FuelLog(
            vehicleId = vehicleId,
            fillDate = fillDate,
            liters = liters,
            pricePerLiter = pricePerLiter,
            totalCost = totalCost,
            odometerAtFill = odometerAtFill,
            fuelType = fuelType,
            kmPerLiter = kmPerLiter
        )
        return fuelLogDao.insertFuelLog(log)
    }

    // The month range is computed inside flow {} so it is re-evaluated on every
    // (re)collection — e.g. when the app returns to the foreground after a month
    // boundary — instead of being frozen at repository-call time.

    /** Total fuel cost (Rupiah) for the current calendar month, 0 when no fills. */
    fun getTotalFuelCostThisMonth(vehicleId: Int): Flow<Int> = flow {
        val (start, end) = monthRange()
        emitAll(fuelLogDao.getTotalFuelCostThisMonth(vehicleId, start, end).map { it ?: 0 })
    }

    /** Average km/L for the current calendar month, 0f when no qualifying fills. */
    fun getAverageKmPerLiterThisMonth(vehicleId: Int): Flow<Float> = flow {
        val (start, end) = monthRange()
        emitAll(fuelLogDao.getAverageKmPerLiterThisMonth(vehicleId, start, end).map { it ?: 0f })
    }
}
