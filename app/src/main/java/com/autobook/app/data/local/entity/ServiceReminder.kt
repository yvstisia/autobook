package com.autobook.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A reminder for the next service, derived from a ServiceRecord.
 * vehicleId is denormalized from ServiceRecord for faster dashboard queries.
 * Cascade-deleted when either its parent ServiceRecord or Vehicle is removed.
 */
@Entity(
    tableName = "service_reminder",
    foreignKeys = [
        ForeignKey(
            entity = ServiceRecord::class,
            parentColumns = ["id"],
            childColumns = ["serviceRecordId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("serviceRecordId"), Index("vehicleId")]
)
data class ServiceReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val serviceRecordId: Int,
    /** denormalized from ServiceRecord for quick query */
    val vehicleId: Int,
    /**
     * Which service type this reminder is for (e.g. "oli", "tune_up", "aki"). A single
     * ServiceRecord can have multiple reminders, one per predictable type. Empty string
     * for legacy/manual reminders that aren't tied to a specific type.
     *
     * defaultValue must match the v1->v2 migration's `DEFAULT ''` so Room's schema
     * validation passes on both fresh installs and upgrades.
     */
    @ColumnInfo(defaultValue = "")
    val serviceType: String = "",
    /** "km", "date", or "both" */
    val remindBy: String,
    /** nullable target odometer */
    val nextKm: Int? = null,
    /** nullable, epoch milliseconds */
    val nextDate: Long? = null,
    /** 0 = active, 1 = done */
    val isDone: Int = 0
)
