package com.autobook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobook.app.data.local.entity.ServiceReminder
import com.autobook.app.data.local.entity.Vehicle
import com.autobook.app.data.repository.FuelRepository
import com.autobook.app.data.repository.ServiceRepository
import com.autobook.app.data.repository.VehicleRepository
import com.autobook.app.ui.UiState
import com.autobook.app.util.ReminderStatus
import com.autobook.app.util.computeReminderStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Per-vehicle summary shown on the dashboard. */
data class DashboardVehicleCard(
    val vehicle: Vehicle,
    /** currentOdometer - latest service odometer; null when no service record exists. */
    val kmSinceLastService: Int?,
    /** null when there is no active reminder. */
    val reminderStatus: ReminderStatus?,
    /** the most-urgent active reminder; its target feeds the "Servis berikutnya" summary. */
    val reminder: ServiceReminder?,
    val fuelCostThisMonth: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    vehicleRepository: VehicleRepository,
    private val serviceRepository: ServiceRepository,
    private val fuelRepository: FuelRepository
) : ViewModel() {

    val dashboardCards: StateFlow<UiState<List<DashboardVehicleCard>>> =
        vehicleRepository.getAllVehicles()
            .flatMapLatest { vehicles ->
                if (vehicles.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val perVehicle = vehicles.map { vehicle -> vehicleCardFlow(vehicle) }
                    combine(perVehicle) { it.toList() }
                }
            }
            .map<List<DashboardVehicleCard>, UiState<List<DashboardVehicleCard>>> { UiState.Success(it) }
            .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private fun vehicleCardFlow(vehicle: Vehicle) = combine(
        serviceRepository.getServiceRecordsByVehicle(vehicle.id),
        serviceRepository.getActiveRemindersByVehicle(vehicle.id),
        fuelRepository.getTotalFuelCostThisMonth(vehicle.id)
    ) { services, reminders, fuelCost ->
        val latest = services.firstOrNull()
        val kmSince = latest?.let { vehicle.currentOdometer - it.odometerAtService }
        val mostUrgent = mostUrgentReminder(reminders, vehicle.currentOdometer)
        DashboardVehicleCard(
            vehicle = vehicle,
            kmSinceLastService = kmSince,
            reminderStatus = mostUrgent?.second,
            reminder = mostUrgent?.first,
            fuelCostThisMonth = fuelCost
        )
    }

    /**
     * Picks the single reminder to surface on the vehicle card: worst status first
     * (overdue > due soon > on track), then the soonest target.
     */
    private fun mostUrgentReminder(
        reminders: List<ServiceReminder>,
        currentOdometer: Int
    ): Pair<ServiceReminder, ReminderStatus>? =
        reminders
            .map { it to computeReminderStatus(currentOdometer, it.nextKm, it.nextDate) }
            .minWithOrNull(
                compareBy<Pair<ServiceReminder, ReminderStatus>> { severityRank(it.second) }
                    .thenBy { it.first.nextDate ?: Long.MAX_VALUE }
                    .thenBy { it.first.nextKm ?: Int.MAX_VALUE }
            )

    private fun severityRank(status: ReminderStatus): Int = when (status) {
        ReminderStatus.OVERDUE -> 0
        ReminderStatus.DUE_SOON -> 1
        ReminderStatus.ON_TRACK -> 2
    }
}
