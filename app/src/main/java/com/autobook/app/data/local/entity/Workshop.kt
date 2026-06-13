package com.autobook.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A bookmarked workshop. Standalone — no foreign key to Vehicle.
 */
@Entity(tableName = "workshop")
data class Workshop(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    /** nullable, free text */
    val address: String? = null,
    /** nullable GPS coordinate */
    val latitude: Double? = null,
    /** nullable GPS coordinate */
    val longitude: Double? = null,
    /** 1-5 stars */
    val rating: Int,
    /** "oli", "ban", "listrik", "body", "umum" (comma-separated for multi) */
    val specialization: String,
    /** nullable, personal notes */
    val notes: String? = null,
    /** epoch milliseconds */
    val savedAt: Long = System.currentTimeMillis()
)
