package com.autobook.app.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.autobook.app.R
import com.autobook.app.ui.viewmodel.ServiceViewModel

@Composable
fun AddServiceScreen(
    viewModel: ServiceViewModel,
    vehicleId: Int,
    vehicleName: String,
    vehicleType: String,
    vehicleCurrentOdometer: Int,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val savedMsg = stringResource(R.string.service_saved)
    ServiceFormScreen(
        titleRes = R.string.add_service_title,
        vehicleId = vehicleId,
        vehicleName = vehicleName,
        vehicleType = vehicleType,
        vehicleCurrentOdometer = vehicleCurrentOdometer,
        initial = null,
        onBack = onBack,
        onSubmit = { record, reminders ->
            viewModel.saveService(record, reminders) {
                onShowMessage(savedMsg)
                onBack()
            }
        }
    )
}
