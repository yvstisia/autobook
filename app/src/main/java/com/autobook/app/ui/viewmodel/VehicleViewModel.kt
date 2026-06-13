package com.autobook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobook.app.data.local.entity.Vehicle
import com.autobook.app.data.repository.VehicleRepository
import com.autobook.app.ui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VehicleViewModel(private val repository: VehicleRepository) : ViewModel() {

    val vehiclesState: StateFlow<UiState<List<Vehicle>>> =
        repository.getAllVehicles()
            .map<List<Vehicle>, UiState<List<Vehicle>>> { UiState.Success(it) }
            .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading
            )

    // DB work runs on Dispatchers.IO; the onDone callback resumes on the main
    // dispatcher so callers can safely navigate / show a snackbar.
    fun insertVehicle(vehicle: Vehicle, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.insertVehicle(vehicle) }
            onDone()
        }
    }

    fun updateVehicle(vehicle: Vehicle, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.updateVehicle(vehicle) }
            onDone()
        }
    }

    fun deleteVehicle(vehicle: Vehicle, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.deleteVehicle(vehicle) }
            onDone()
        }
    }

    /** Loads a single vehicle for the edit screen; result delivered via [onResult]. */
    fun loadVehicle(id: Int, onResult: (Vehicle?) -> Unit) {
        viewModelScope.launch {
            val vehicle = withContext(Dispatchers.IO) { repository.getVehicleById(id) }
            onResult(vehicle)
        }
    }
}
