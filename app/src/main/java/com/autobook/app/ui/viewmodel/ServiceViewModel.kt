package com.autobook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobook.app.data.local.entity.ServiceRecord
import com.autobook.app.data.local.entity.ServiceReminder
import com.autobook.app.data.repository.ServiceRepository
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
class ServiceViewModel(private val repository: ServiceRepository) : ViewModel() {

    private val _selectedVehicleId = MutableStateFlow<Int?>(null)
    val selectedVehicleId: StateFlow<Int?> = _selectedVehicleId.asStateFlow()

    val serviceRecords: StateFlow<List<ServiceRecord>> =
        _selectedVehicleId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getServiceRecordsByVehicle(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeReminder: StateFlow<ServiceReminder?> =
        _selectedVehicleId.flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getActiveReminderByVehicle(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setSelectedVehicle(id: Int) {
        _selectedVehicleId.value = id
    }

    fun markReminderDone(reminderId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.markReminderDone(reminderId) }
            onDone()
        }
    }

    /**
     * Inserts a service record and, if [reminderDraft] is non-null, an attached
     * reminder. The new record id is linked into the reminder before insert.
     * Any previously active reminders for the vehicle are marked done first —
     * a new service fulfills them, otherwise the daily worker would keep
     * notifying about stale reminders forever.
     */
    fun saveService(
        record: ServiceRecord,
        reminderDraft: ServiceReminder?,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.markAllRemindersDoneForVehicle(record.vehicleId)
                val newId = repository.insertServiceRecord(record).toInt()
                if (reminderDraft != null) {
                    repository.insertReminder(
                        reminderDraft.copy(
                            serviceRecordId = newId,
                            vehicleId = record.vehicleId
                        )
                    )
                }
            }
            onDone()
        }
    }
}
