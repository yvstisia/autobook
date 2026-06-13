package com.autobook.app.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.autobook.app.R
import com.autobook.app.ui.component.VehicleForm
import com.autobook.app.ui.viewmodel.VehicleViewModel

@Composable
fun AddVehicleScreen(
    viewModel: VehicleViewModel,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val savedMsg = stringResource(R.string.vehicle_saved)
    VehicleForm(
        title = stringResource(R.string.add_vehicle_title),
        initial = null,
        onBack = onBack,
        onSave = { vehicle ->
            viewModel.insertVehicle(vehicle) {
                onShowMessage(savedMsg)
                onBack()
            }
        }
    )
}
