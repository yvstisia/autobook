package com.autobook.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.autobook.app.R
import com.autobook.app.data.local.entity.Vehicle
import com.autobook.app.ui.component.VehicleForm
import com.autobook.app.ui.viewmodel.VehicleViewModel

@Composable
fun EditVehicleScreen(
    viewModel: VehicleViewModel,
    vehicleId: Int,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    var vehicle by remember { mutableStateOf<Vehicle?>(null) }

    LaunchedEffect(vehicleId) {
        viewModel.loadVehicle(vehicleId) { vehicle = it }
    }

    val savedMsg = stringResource(R.string.vehicle_saved)
    val deletedMsg = stringResource(R.string.vehicle_deleted)

    val loaded = vehicle
    if (loaded == null) {
        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    } else {
        VehicleForm(
            title = stringResource(R.string.edit_vehicle_title),
            initial = loaded,
            onBack = onBack,
            onSave = { updated ->
                viewModel.updateVehicle(updated) {
                    onShowMessage(savedMsg)
                    onBack()
                }
            },
            onDelete = {
                viewModel.deleteVehicle(loaded) {
                    onShowMessage(deletedMsg)
                    onBack()
                }
            }
        )
    }
}
