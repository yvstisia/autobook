package com.autobook.app

import android.content.Context
import com.autobook.app.data.local.AppDatabase
import com.autobook.app.data.preferences.UserPreferencesRepository
import com.autobook.app.data.repository.FuelRepository
import com.autobook.app.data.repository.ServiceRepository
import com.autobook.app.data.repository.VehicleRepository
import com.autobook.app.data.repository.WorkshopRepository

/**
 * Manual dependency-injection container. Holds singleton instances of the database
 * and all repositories. Created once in [AutoBookApplication].
 */
class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.getInstance(context)

    val vehicleRepository: VehicleRepository by lazy {
        VehicleRepository(database.vehicleDao())
    }

    val serviceRepository: ServiceRepository by lazy {
        ServiceRepository(database.serviceRecordDao(), database.serviceReminderDao())
    }

    val fuelRepository: FuelRepository by lazy {
        FuelRepository(database.fuelLogDao())
    }

    val workshopRepository: WorkshopRepository by lazy {
        WorkshopRepository(database.workshopDao())
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }
}
