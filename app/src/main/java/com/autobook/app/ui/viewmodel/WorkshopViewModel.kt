package com.autobook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobook.app.data.local.entity.Workshop
import com.autobook.app.data.repository.WorkshopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkshopViewModel(private val repository: WorkshopRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Workshops filtered by [searchQuery] (name contains query, case-insensitive). */
    val workshops: StateFlow<List<Workshop>> =
        combine(repository.getAllWorkshops(), _searchQuery) { list, query ->
            if (query.isBlank()) list
            else list.filter { it.name.contains(query.trim(), ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertWorkshop(workshop: Workshop, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.insertWorkshop(workshop) }
            onDone()
        }
    }

    fun updateWorkshop(workshop: Workshop, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.updateWorkshop(workshop) }
            onDone()
        }
    }

    fun deleteWorkshop(workshop: Workshop, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.deleteWorkshop(workshop) }
            onDone()
        }
    }

    fun loadWorkshop(id: Int, onResult: (Workshop?) -> Unit) {
        viewModelScope.launch {
            val workshop = withContext(Dispatchers.IO) { repository.getWorkshopById(id) }
            onResult(workshop)
        }
    }
}
