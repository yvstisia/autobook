package com.autobook.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobook.app.R
import com.autobook.app.data.local.entity.Vehicle
import com.autobook.app.ui.UiState
import com.autobook.app.ui.component.AutoBookCard
import com.autobook.app.ui.component.AutoBookFab
import com.autobook.app.ui.component.EmptyState
import com.autobook.app.ui.component.VehicleIconChip
import com.autobook.app.ui.theme.BottomNavContentPadding
import com.autobook.app.ui.theme.CardGap
import com.autobook.app.ui.theme.ScreenPaddingH
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.ui.theme.numberMedium
import com.autobook.app.ui.viewmodel.VehicleViewModel
import com.autobook.app.util.LocalAppFormatter

@Composable
fun VehicleListScreen(
    viewModel: VehicleViewModel,
    onAddVehicle: () -> Unit,
    onVehicleClick: (Int) -> Unit
) {
    val state by viewModel.vehiclesState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = { AutoBookFab(onClick = onAddVehicle) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Error -> Text(
                    text = s.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = autoBookColors.textSecondary,
                    modifier = Modifier.align(Alignment.Center).padding(ScreenPaddingH)
                )
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            ScreenTitle()
                            EmptyState(
                                icon = Icons.Outlined.DirectionsCar,
                                title = stringResource(R.string.vehicle_empty_title),
                                subtitle = stringResource(R.string.vehicle_empty_subtitle)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = ScreenPaddingH,
                                end = ScreenPaddingH,
                                bottom = BottomNavContentPadding
                            )
                        ) {
                            item { ScreenTitle() }
                            items(s.data, key = { it.id }) { vehicle ->
                                VehicleRow(
                                    vehicle = vehicle,
                                    onClick = { onVehicleClick(vehicle.id) },
                                    modifier = Modifier.padding(bottom = CardGap)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenTitle() {
    Text(
        text = stringResource(R.string.vehicle_list_title),
        style = MaterialTheme.typography.headlineLarge,
        color = autoBookColors.textPrimary,
        modifier = Modifier.padding(top = 20.dp, bottom = 16.dp, start = 0.dp)
    )
}

@Composable
private fun VehicleRow(vehicle: Vehicle, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val fmt = LocalAppFormatter.current
    AutoBookCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VehicleIconChip(type = vehicle.type)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = vehicle.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    color = autoBookColors.textPrimary
                )
                Text(
                    text = "${vehicle.brand} ${vehicle.model} · ${vehicle.year}",
                    style = MaterialTheme.typography.labelMedium,
                    color = autoBookColors.textSecondary
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = fmt.odometer(vehicle.currentOdometer),
                        style = numberMedium,
                        color = autoBookColors.textPrimary
                    )
                    Text(
                        text = " " + stringResource(R.string.unit_km),
                        style = MaterialTheme.typography.labelSmall,
                        color = autoBookColors.textSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = autoBookColors.textTertiary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
