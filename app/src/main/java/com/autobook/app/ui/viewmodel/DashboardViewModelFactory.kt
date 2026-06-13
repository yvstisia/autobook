package com.autobook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autobook.app.data.repository.FuelRepository
import com.autobook.app.data.repository.ServiceRepository
import com.autobook.app.data.repository.VehicleRepository

class DashboardViewModelFactory(
    private val vehicleRepository: VehicleRepository,
    private val serviceRepository: ServiceRepository,
    private val fuelRepository: FuelRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(vehicleRepository, serviceRepository, fuelRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
