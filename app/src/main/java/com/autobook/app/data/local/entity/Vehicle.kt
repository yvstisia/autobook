package com.autobook.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A vehicle owned by the user (motor or mobil).
 * Root entity — deleting it cascade-deletes all related records.
 */
@Entity(tableName = "vehicle")
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nickname: String,
    /** "motor" or "mobil" */
    val type: String,
    val brand: String,
    val model: String,
    val year: Int,
    /** in km, updated manually */
    val currentOdometer: Int,
    /** nullable local file path */
    val photoPath: String? = null,
    /** epoch milliseconds */
    val createdAt: Long = System.currentTimeMillis()
)
