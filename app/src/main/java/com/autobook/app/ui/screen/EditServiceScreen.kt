package com.autobook.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.autobook.app.R
import com.autobook.app.data.local.entity.ServiceRecord
import com.autobook.app.data.local.entity.Vehicle
import com.autobook.app.ui.viewmodel.ServiceViewModel

@Composable
fun EditServiceScreen(
    viewModel: ServiceViewModel,
    recordId: Int,
    vehicles: List<Vehicle>,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    var record by remember { mutableStateOf<ServiceRecord?>(null) }
    LaunchedEffect(recordId) { viewModel.loadService(recordId) { record = it } }

    val savedMsg = stringResource(R.string.service_saved)
    val deletedMsg = stringResource(R.string.service_deleted)

    val loaded = record
    if (loaded == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    } else {
        val vehicle = vehicles.firstOrNull { it.id == loaded.vehicleId }
        ServiceFormScreen(
            titleRes = R.string.edit_service_title,
            vehicleId = loaded.vehicleId,
            vehicleName = vehicle?.nickname ?: "",
            vehicleType = vehicle?.type ?: "motor",
            initial = loaded,
            onBack = onBack,
            onSubmit = { rec, reminders ->
                viewModel.updateService(rec, reminders) {
                    onShowMessage(savedMsg)
                    onBack()
                }
            },
            onDelete = {
                viewModel.deleteService(loaded) {
                    onShowMessage(deletedMsg)
                    onBack()
                }
            }
        )
    }
}
