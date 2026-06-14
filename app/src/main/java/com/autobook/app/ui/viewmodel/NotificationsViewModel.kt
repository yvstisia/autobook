package com.autobook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobook.app.data.local.entity.ServiceReminder
import com.autobook.app.data.repository.ServiceRepository
import com.autobook.app.data.repository.VehicleRepository
import com.autobook.app.util.ReminderStatus
import com.autobook.app.util.computeReminderStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** A single in-app notification, derived from a due-soon / overdue service reminder. */
data class NotificationItem(
    val vehicleName: String,
    val serviceType: String,
    val status: ReminderStatus,
    val reminder: ServiceReminder
)

/**
 * Backs the bell inbox. Notifications are derived live from active reminders that are
 * due-soon or overdue (relative to each vehicle's current odometer / today) — no separate
 * storage. Overdue items are listed first.
 */
class NotificationsViewModel(
    vehicleRepository: VehicleRepository,
    serviceRepository: ServiceRepository
) : ViewModel() {

    val notifications: StateFlow<List<NotificationItem>> = combine(
        vehicleRepository.getAllVehicles(),
        serviceRepository.getAllActiveRemindersFlow()
    ) { vehicles, reminders ->
        val vehicleById = vehicles.associateBy { it.id }
        reminders.mapNotNull { reminder ->
            val vehicle = vehicleById[reminder.vehicleId] ?: return@mapNotNull null
            val status = computeReminderStatus(vehicle.currentOdometer, reminder.nextKm, reminder.nextDate)
            if (status == ReminderStatus.ON_TRACK) {
                null
            } else {
                NotificationItem(vehicle.nickname, reminder.serviceType, status, reminder)
            }
        }.sortedBy { if (it.status == ReminderStatus.OVERDUE) 0 else 1 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
