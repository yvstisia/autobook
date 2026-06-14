package com.autobook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autobook.app.data.repository.FuelRepository
import com.autobook.app.data.repository.VehicleRepository

class FuelViewModelFactory(
    private val repository: FuelRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FuelViewModel::class.java)) {
            return FuelViewModel(repository, vehicleRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
