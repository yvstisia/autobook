package com.autobook.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single fuel fill-up for a vehicle.
 * kmPerLiter is calculated at insert time (see FuelRepository), not on-the-fly.
 * Cascade-deleted when its parent Vehicle is removed.
 */
@Entity(
    tableName = "fuel_log",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehicleId")]
)
data class FuelLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val vehicleId: Int,
    /** epoch milliseconds */
    val fillDate: Long,
    val liters: Float,
    /** Rupiah */
    val pricePerLiter: Int,
    /** Rupiah = liters * pricePerLiter, rounded */
    val totalCost: Int,
    val odometerAtFill: Int,
    /** "Pertalite", "Pertamax", "Pertamax Turbo", "Solar", "Dexlite" */
    val fuelType: String,
    /** auto-calculated: (odometerAtFill - previous odometerAtFill) / liters; 0.0 if first entry */
    val kmPerLiter: Float = 0.0f
)
