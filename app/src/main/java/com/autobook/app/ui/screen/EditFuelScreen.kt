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
import com.autobook.app.data.local.entity.FuelLog
import com.autobook.app.data.local.entity.Vehicle
import com.autobook.app.ui.viewmodel.FuelViewModel

@Composable
fun EditFuelScreen(
    viewModel: FuelViewModel,
    logId: Int,
    vehicles: List<Vehicle>,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    var log by remember { mutableStateOf<FuelLog?>(null) }
    LaunchedEffect(logId) { viewModel.loadFuelLog(logId) { log = it } }

    val savedMsg = stringResource(R.string.fuel_saved)
    val deletedMsg = stringResource(R.string.fuel_deleted)

    val loaded = log
    if (loaded == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    } else {
        val vehicle = vehicles.firstOrNull { it.id == loaded.vehicleId }
        FuelFormScreen(
            titleRes = R.string.edit_fuel_title,
            vehicleName = vehicle?.nickname ?: "",
            vehicleCurrentOdometer = vehicle?.currentOdometer ?: 0,
            initial = loaded,
            onBack = onBack,
            onSubmit = { fillDate, liters, priceMinor, odometerAtFill, fuelType ->
                viewModel.updateFuelLog(
                    id = loaded.id,
                    vehicleId = loaded.vehicleId,
                    fillDate = fillDate,
                    liters = liters,
                    pricePerLiter = priceMinor,
                    odometerAtFill = odometerAtFill,
                    fuelType = fuelType
                ) {
                    onShowMessage(savedMsg)
                    onBack()
                }
            },
            onDelete = {
                viewModel.deleteFuelLog(loaded) {
                    onShowMessage(deletedMsg)
                    onBack()
                }
            }
        )
    }
}
