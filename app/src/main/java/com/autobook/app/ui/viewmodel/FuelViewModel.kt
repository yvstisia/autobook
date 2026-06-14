package com.autobook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobook.app.data.local.entity.FuelLog
import com.autobook.app.data.repository.FuelRepository
import com.autobook.app.data.repository.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class FuelViewModel(
    private val repository: FuelRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _selectedVehicleId = MutableStateFlow<Int?>(null)
    val selectedVehicleId: StateFlow<Int?> = _selectedVehicleId.asStateFlow()

    val fuelLogs: StateFlow<List<FuelLog>> =
        _selectedVehicleId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getFuelLogsByVehicle(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthlyTotal: StateFlow<Int> =
        _selectedVehicleId.flatMapLatest { id ->
            if (id == null) flowOf(0) else repository.getTotalFuelCostThisMonth(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val monthlyAvgKmPerLiter: StateFlow<Float> =
        _selectedVehicleId.flatMapLatest { id ->
            if (id == null) flowOf(0f) else repository.getAverageKmPerLiterThisMonth(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    fun setSelectedVehicle(id: Int) {
        _selectedVehicleId.value = id
    }

    /** totalCost and kmPerLiter are computed inside FuelRepository. */
    fun insertFuelLog(
        vehicleId: Int,
        fillDate: Long,
        liters: Float,
        pricePerLiter: Int,
        odometerAtFill: Int,
        fuelType: String,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertFuelLog(vehicleId, fillDate, liters, pricePerLiter, odometerAtFill, fuelType)
                vehicleRepository.bumpOdometerIfHigher(vehicleId, odometerAtFill)
            }
            onDone()
        }
    }

    /** Loads a single fuel log for the edit screen. */
    fun loadFuelLog(id: Int, onResult: (FuelLog?) -> Unit) {
        viewModelScope.launch {
            val log = withContext(Dispatchers.IO) { repository.getFuelLogById(id) }
            onResult(log)
        }
    }

    /** Updates a fuel log; totalCost and km/L (this entry + dependents) are recomputed. */
    fun updateFuelLog(
        id: Int,
        vehicleId: Int,
        fillDate: Long,
        liters: Float,
        pricePerLiter: Int,
        odometerAtFill: Int,
        fuelType: String,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.updateFuelLog(id, vehicleId, fillDate, liters, pricePerLiter, odometerAtFill, fuelType)
                vehicleRepository.bumpOdometerIfHigher(vehicleId, odometerAtFill)
            }
            onDone()
        }
    }

    fun deleteFuelLog(log: FuelLog, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.deleteFuelLog(log) }
            onDone()
        }
    }
}
