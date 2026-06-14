package com.autobook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobook.app.data.local.entity.ServiceRecord
import com.autobook.app.data.local.entity.ServiceReminder
import com.autobook.app.data.local.entity.Workshop
import com.autobook.app.data.repository.ServiceRepository
import com.autobook.app.data.repository.VehicleRepository
import com.autobook.app.data.repository.WorkshopRepository
import com.autobook.app.util.AutoReminderCalculator
import com.autobook.app.util.splitCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceViewModel(
    private val repository: ServiceRepository,
    private val vehicleRepository: VehicleRepository,
    private val workshopRepository: WorkshopRepository
) : ViewModel() {

    private val _selectedVehicleId = MutableStateFlow<Int?>(null)
    val selectedVehicleId: StateFlow<Int?> = _selectedVehicleId.asStateFlow()

    val serviceRecords: StateFlow<List<ServiceRecord>> =
        _selectedVehicleId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getServiceRecordsByVehicle(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeReminders: StateFlow<List<ServiceReminder>> =
        _selectedVehicleId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getActiveRemindersByVehicle(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Existing workshop names, used to suggest while typing the workshop in the service form. */
    val workshopNames: StateFlow<List<String>> =
        workshopRepository.getAllWorkshops()
            .map { list -> list.map { it.name } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
     * Inserts a service record and any attached [reminders] (one per predictable service
     * type in Automatic mode, a single custom reminder in Manual mode, or none). Each
     * reminder is linked to the new record id before insert. Previously active reminders
     * for the vehicle are marked done first — a new service fulfills them, otherwise the
     * daily worker would keep notifying about stale reminders forever.
     */
    fun saveService(
        record: ServiceRecord,
        reminders: List<ServiceReminder>,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Fulfill existing reminders only for the predictable types actually serviced
                // (plus the types of any new reminders). Unrelated logs — e.g. a tire change —
                // leave existing reminders untouched.
                val typesToFulfill = (
                    splitCodes(record.serviceTypes).filter { it in AutoReminderCalculator.PREDICTABLE_TYPES } +
                        reminders.map { it.serviceType }
                    ).toSet()
                typesToFulfill.forEach { repository.markRemindersDoneForType(record.vehicleId, it) }

                val newId = repository.insertServiceRecord(record).toInt()
                reminders.forEach { draft ->
                    repository.insertReminder(
                        draft.copy(serviceRecordId = newId, vehicleId = record.vehicleId)
                    )
                }
                vehicleRepository.bumpOdometerIfHigher(record.vehicleId, record.odometerAtService)
                syncWorkshop(record.workshopName)
            }
            onDone()
        }
    }

    /** Loads a single service record for the edit screen. */
    fun loadService(id: Int, onResult: (ServiceRecord?) -> Unit) {
        viewModelScope.launch {
            val record = withContext(Dispatchers.IO) { repository.getServiceRecordById(id) }
            onResult(record)
        }
    }

    /**
     * Updates a service record and regenerates its reminders (old ones for this record are
     * removed, then the new [reminders] are inserted linked to it).
     */
    fun updateService(
        record: ServiceRecord,
        reminders: List<ServiceReminder>,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.updateServiceRecord(record)
                repository.deleteRemindersForRecord(record.id)
                reminders.forEach { draft ->
                    repository.insertReminder(
                        draft.copy(serviceRecordId = record.id, vehicleId = record.vehicleId)
                    )
                }
                vehicleRepository.bumpOdometerIfHigher(record.vehicleId, record.odometerAtService)
                syncWorkshop(record.workshopName)
            }
            onDone()
        }
    }

    /** Deletes a service record; its reminders are cascade-deleted by the database. */
    fun deleteService(record: ServiceRecord, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.deleteServiceRecord(record) }
            onDone()
        }
    }

    /**
     * Ensures a workshop with [name] exists in the Workshop list (case-insensitive), creating
     * it once if needed. Visit counts are derived from service records, so no counter to bump.
     */
    private suspend fun syncWorkshop(name: String?) {
        val trimmed = name?.trim()
        if (trimmed.isNullOrBlank()) return
        if (workshopRepository.findWorkshopByName(trimmed) == null) {
            workshopRepository.insertWorkshop(
                Workshop(name = trimmed, rating = 0, specialization = "")
            )
        }
    }
}
