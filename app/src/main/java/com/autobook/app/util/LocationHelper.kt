package com.autobook.app.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Thin wrapper around FusedLocationProviderClient. The caller is responsible for
 * ensuring ACCESS_FINE/COARSE_LOCATION has been granted before calling this.
 */
@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(context: Context): Location? =
    suspendCancellableCoroutine { cont ->
        val client = LocationServices.getFusedLocationProviderClient(context)
        // Prefer last known location; fall back to a fresh fix if none is cached.
        client.lastLocation
            .addOnSuccessListener { last ->
                if (last != null) {
                    cont.resume(last)
                } else {
                    client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { fresh -> cont.resume(fresh) }
                        .addOnFailureListener { cont.resume(null) }
                }
            }
            .addOnFailureListener { cont.resume(null) }
    }
