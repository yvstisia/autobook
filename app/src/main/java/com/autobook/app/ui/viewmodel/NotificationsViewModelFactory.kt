package com.autobook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autobook.app.data.repository.ServiceRepository
import com.autobook.app.data.repository.VehicleRepository

class NotificationsViewModelFactory(
    private val vehicleRepository: VehicleRepository,
    private val serviceRepository: ServiceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            return NotificationsViewModel(vehicleRepository, serviceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
