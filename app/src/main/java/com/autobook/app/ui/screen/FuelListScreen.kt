package com.autobook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobook.app.R
import com.autobook.app.data.local.entity.FuelLog
import com.autobook.app.data.local.entity.Vehicle
import com.autobook.app.ui.component.AutoBookCard
import com.autobook.app.ui.component.AutoBookFab
import com.autobook.app.ui.component.EmptyState
import com.autobook.app.ui.component.SummaryCard
import com.autobook.app.ui.component.VehicleFilterChips
import com.autobook.app.ui.theme.BottomNavContentPadding
import com.autobook.app.ui.theme.CardGap
import com.autobook.app.ui.theme.RadiusIconChip
import com.autobook.app.ui.theme.ScreenPaddingH
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.ui.theme.numberMedium
import com.autobook.app.ui.viewmodel.FuelViewModel
import com.autobook.app.util.formatDate
import com.autobook.app.util.formatKmPerLiter
import com.autobook.app.util.formatLiters
import com.autobook.app.util.formatRupiah

@Composable
fun FuelListScreen(
    viewModel: FuelViewModel,
    vehicles: List<Vehicle>,
    onAddFuel: (Int) -> Unit
) {
    val selectedId by viewModel.selectedVehicleId.collectAsStateWithLifecycle()
    val logs by viewModel.fuelLogs.collectAsStateWithLifecycle()
    val monthlyTotal by viewModel.monthlyTotal.collectAsStateWithLifecycle()
    val avgKmpl by viewModel.monthlyAvgKmPerLiter.collectAsStateWithLifecycle()

    LaunchedEffect(vehicles, selectedId) {
        if (selectedId == null && vehicles.isNotEmpty()) {
            viewModel.setSelectedVehicle(vehicles.first().id)
        }
    }

    val selectedVehicle = vehicles.firstOrNull { it.id == selectedId }

    Scaffold(
        floatingActionButton = {
            if (selectedVehicle != null) {
                AutoBookFab(onClick = { onAddFuel(selectedVehicle.id) })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (vehicles.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.LocalGasStation,
                    title = stringResource(R.string.fuel_empty_title),
                    subtitle = stringResource(R.string.no_vehicle_to_service)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.fuel_list_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = autoBookColors.textPrimary,
                        modifier = Modifier.padding(start = ScreenPaddingH, top = 20.dp, bottom = 8.dp)
                    )
                    VehicleFilterChips(
                        vehicles = vehicles,
                        selectedVehicleId = selectedId,
                        onSelect = viewModel::setSelectedVehicle
                    )

                    if (logs.isEmpty()) {
                        // Plain Column so the empty state can actually center —
                        // fillMaxSize inside a LazyColumn item has no effect.
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = ScreenPaddingH)
                                .padding(top = 8.dp)
                        ) {
                            MonthlySummaryCard(monthlyTotal, avgKmpl)
                            EmptyState(
                                icon = Icons.Outlined.LocalGasStation,
                                title = stringResource(R.string.fuel_empty_title),
                                subtitle = stringResource(R.string.fuel_empty_subtitle),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = ScreenPaddingH,
                                end = ScreenPaddingH,
                                top = 8.dp,
                                bottom = BottomNavContentPadding
                            )
                        ) {
                            item { MonthlySummaryCard(monthlyTotal, avgKmpl) }
                            items(logs, key = { it.id }) { log ->
                                FuelCard(log = log, modifier = Modifier.padding(bottom = CardGap))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(monthlyTotal: Int, avgKmpl: Float) {
    SummaryCard.Filled(
        label = stringResource(R.string.summary_fuel_month),
        value = formatRupiah(monthlyTotal),
        supportingText = if (avgKmpl > 0f) {
            stringResource(R.string.monthly_avg_kmpl, formatKmPerLiter(avgKmpl))
        } else null,
        modifier = Modifier.fillMaxWidth().padding(bottom = CardGap)
    )
}

@Composable
private fun FuelCard(log: FuelLog, modifier: Modifier = Modifier) {
    val colors = autoBookColors
    AutoBookCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RadiusIconChip)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalGasStation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = "${log.fuelType} · ${formatLiters(log.liters)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = formatDate(log.fillDate),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatRupiah(log.totalCost),
                    style = numberMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = formatKmPerLiter(log.kmPerLiter),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (log.kmPerLiter > 0f) colors.success else colors.textTertiary
                )
            }
        }
    }
}
