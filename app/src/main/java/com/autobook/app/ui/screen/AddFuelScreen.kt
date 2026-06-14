package com.autobook.app.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.autobook.app.R
import com.autobook.app.ui.viewmodel.FuelViewModel

@Composable
fun AddFuelScreen(
    viewModel: FuelViewModel,
    vehicleId: Int,
    vehicleName: String,
    vehicleCurrentOdometer: Int,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val savedMsg = stringResource(R.string.fuel_saved)
    FuelFormScreen(
        titleRes = R.string.add_fuel_title,
        vehicleName = vehicleName,
        vehicleCurrentOdometer = vehicleCurrentOdometer,
        initial = null,
        onBack = onBack,
        onSubmit = { fillDate, liters, priceMinor, odometerAtFill, fuelType ->
            viewModel.insertFuelLog(
                vehicleId = vehicleId,
                fillDate = fillDate,
                liters = liters,
                pricePerLiter = priceMinor,
                odometerAtFill = odometerAtFill,
                fuelType = fuelType
            ) {
                onShowMessage(savedMsg)
                onBack()
            }
        }
    )
}
