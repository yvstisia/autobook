package com.autobook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobook.app.R
import com.autobook.app.ui.UiState
import com.autobook.app.ui.component.AutoBookCard
import com.autobook.app.ui.component.EmptyState
import com.autobook.app.ui.component.SectionHeader
import com.autobook.app.ui.component.StatusBadge
import com.autobook.app.ui.component.SummaryCard
import com.autobook.app.ui.component.TonalActionButton
import com.autobook.app.ui.component.VehicleIconChip
import com.autobook.app.ui.theme.BottomNavContentPadding
import com.autobook.app.ui.theme.CardGap
import com.autobook.app.ui.theme.RadiusButton
import com.autobook.app.ui.theme.ScreenPaddingH
import com.autobook.app.ui.theme.SectionGap
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.ui.theme.numberMedium
import com.autobook.app.ui.viewmodel.DashboardVehicleCard
import com.autobook.app.ui.viewmodel.DashboardViewModel
import com.autobook.app.util.formatDate
import com.autobook.app.util.formatDayDate
import com.autobook.app.util.formatOdometer
import com.autobook.app.util.formatRupiah

private enum class QuickAction { SERVICE, FUEL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddFirstVehicle: () -> Unit,
    onSeeAllVehicles: () -> Unit,
    onAddService: (Int) -> Unit,
    onAddFuel: (Int) -> Unit
) {
    val state by viewModel.dashboardCards.collectAsStateWithLifecycle()
    var pendingAction by remember { mutableStateOf<QuickAction?>(null) }

    Scaffold { padding ->
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
                    val cards = s.data
                    if (cards.isEmpty()) {
                        DashboardEmpty(onAddFirstVehicle)
                    } else {
                        DashboardContent(
                            cards = cards,
                            onSeeAllVehicles = onSeeAllVehicles,
                            onQuickService = {
                                if (cards.size == 1) onAddService(cards.first().vehicle.id)
                                else pendingAction = QuickAction.SERVICE
                            },
                            onQuickFuel = {
                                if (cards.size == 1) onAddFuel(cards.first().vehicle.id)
                                else pendingAction = QuickAction.FUEL
                            }
                        )

                        if (pendingAction != null) {
                            ModalBottomSheet(onDismissRequest = { pendingAction = null }) {
                                Column(Modifier.fillMaxWidth().padding(ScreenPaddingH)) {
                                    Text(
                                        text = stringResource(R.string.pick_vehicle),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = autoBookColors.textPrimary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    cards.forEach { card ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val action = pendingAction
                                                    pendingAction = null
                                                    when (action) {
                                                        QuickAction.SERVICE -> onAddService(card.vehicle.id)
                                                        QuickAction.FUEL -> onAddFuel(card.vehicle.id)
                                                        null -> {}
                                                    }
                                                }
                                                .padding(vertical = 10.dp)
                                        ) {
                                            VehicleIconChip(type = card.vehicle.type, size = 36.dp)
                                            Text(
                                                text = card.vehicle.nickname,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = autoBookColors.textPrimary,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardEmpty(onAddFirstVehicle: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(ScreenPaddingH),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmptyState(
            icon = Icons.Outlined.DirectionsCar,
            title = stringResource(R.string.dashboard_empty_title),
            subtitle = stringResource(R.string.dashboard_empty_subtitle),
            modifier = Modifier.weight(1f, fill = false)
        )
        Button(
            onClick = onAddFirstVehicle,
            shape = RadiusButton,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.padding(top = 16.dp).height(52.dp)
        ) {
            Text(stringResource(R.string.add_first_vehicle), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun DashboardContent(
    cards: List<DashboardVehicleCard>,
    onSeeAllVehicles: () -> Unit,
    onQuickService: () -> Unit,
    onQuickFuel: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ScreenPaddingH,
            end = ScreenPaddingH,
            top = 20.dp,
            bottom = BottomNavContentPadding
        )
    ) {
        item { DashboardHeader() }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = SectionGap),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard.Filled(
                    label = stringResource(R.string.summary_fuel_month),
                    value = formatRupiah(cards.sumOf { it.fuelCostThisMonth }),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard.Outlined(
                    label = stringResource(R.string.summary_next_service),
                    value = nearestReminderText(cards),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.my_vehicles),
                actionLabel = stringResource(R.string.see_all),
                onAction = onSeeAllVehicles,
                modifier = Modifier.padding(top = SectionGap, bottom = CardGap)
            )
        }

        items(cards, key = { it.vehicle.id }) { card ->
            DashboardVehicleRow(card = card, modifier = Modifier.padding(bottom = CardGap))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = CardGap),
                horizontalArrangement = Arrangement.spacedBy(CardGap)
            ) {
                TonalActionButton(
                    icon = Icons.Outlined.Build,
                    label = stringResource(R.string.quick_add_service),
                    onClick = onQuickService,
                    modifier = Modifier.weight(1f)
                )
                TonalActionButton(
                    icon = Icons.Outlined.LocalGasStation,
                    label = stringResource(R.string.quick_add_fuel),
                    onClick = onQuickFuel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = formatDayDate(System.currentTimeMillis()),
                style = MaterialTheme.typography.labelMedium,
                color = autoBookColors.textSecondary
            )
            Text(
                text = stringResource(R.string.dashboard_greeting),
                style = MaterialTheme.typography.headlineLarge,
                color = autoBookColors.textPrimary
            )
        }
        // Notification affordance (non-functional placeholder per DESIGN.md §5.1).
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = stringResource(R.string.cd_notifications),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DashboardVehicleRow(card: DashboardVehicleCard, modifier: Modifier = Modifier) {
    val v = card.vehicle
    AutoBookCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VehicleIconChip(type = v.type)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = v.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    color = autoBookColors.textPrimary
                )
                Text(
                    text = "${v.brand} ${v.model} · ${v.year}",
                    style = MaterialTheme.typography.labelMedium,
                    color = autoBookColors.textSecondary
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatOdometer(v.currentOdometer),
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
            StatusBadge(status = card.reminderStatus)
        }
    }
}

/** "X km lagi", a date, or "Belum diset" — nearest target across all vehicles. */
@Composable
private fun nearestReminderText(cards: List<DashboardVehicleCard>): String {
    val kmCandidates = cards.mapNotNull { card ->
        card.reminder?.nextKm?.let { it - card.vehicle.currentOdometer }
    }
    val dateCandidates = cards.mapNotNull { it.reminder?.nextDate }

    return when {
        kmCandidates.isNotEmpty() ->
            stringResource(R.string.km_remaining, formatOdometer(kmCandidates.min().coerceAtLeast(0)))
        dateCandidates.isNotEmpty() -> formatDate(dateCandidates.min())
        else -> stringResource(R.string.not_set)
    }
}
