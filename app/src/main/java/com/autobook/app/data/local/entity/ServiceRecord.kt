package com.autobook.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single service event for a vehicle.
 * Cascade-deleted when its parent Vehicle is removed.
 */
@Entity(
    tableName = "service_record",
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
data class ServiceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val vehicleId: Int,
    /** epoch milliseconds */
    val serviceDate: Long,
    val odometerAtService: Int,
    /** comma-separated, e.g. "oli,tune_up,rem" */
    val serviceTypes: String,
    /** Rupiah, integer only */
    val cost: Int,
    val notes: String? = null,
    /** Optional workshop name where the service was done; synced into the Workshop list. */
    val workshopName: String? = null
)
