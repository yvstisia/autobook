package com.autobook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.Settings
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
import com.autobook.app.util.LocalAppFormatter
import com.autobook.app.util.ReminderStatus

private enum class QuickAction { SERVICE, FUEL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userName: String,
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
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
                        DashboardEmpty(onAddFirstVehicle, onOpenSettings, onOpenNotifications)
                    } else {
                        DashboardContent(
                            cards = cards,
                            userName = userName,
                            onOpenSettings = onOpenSettings,
                            onOpenNotifications = onOpenNotifications,
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
private fun DashboardEmpty(
    onAddFirstVehicle: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Keep Settings and Notifications reachable even before any vehicle exists.
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = ScreenPaddingH),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeaderIconButton(
                icon = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.cd_settings),
                onClick = onOpenSettings
            )
            HeaderIconButton(
                icon = Icons.Outlined.Notifications,
                contentDescription = stringResource(R.string.cd_notifications),
                onClick = onOpenNotifications
            )
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(ScreenPaddingH),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmptyStateInner(onAddFirstVehicle)
        }
    }
}

@Composable
private fun EmptyStateInner(onAddFirstVehicle: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
    userName: String,
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSeeAllVehicles: () -> Unit,
    onQuickService: () -> Unit,
    onQuickFuel: () -> Unit
) {
    val fmt = LocalAppFormatter.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ScreenPaddingH,
            end = ScreenPaddingH,
            top = 20.dp,
            bottom = BottomNavContentPadding
        )
    ) {
        item {
            DashboardHeader(
                userName = userName,
                onOpenSettings = onOpenSettings,
                onOpenNotifications = onOpenNotifications
            )
        }

        item {
            val urgentCard = mostUrgentCard(cards)
            val nextServiceLabel = if (urgentCard != null) {
                "${stringResource(R.string.summary_next_service)} - ${urgentCard.vehicle.nickname}"
            } else {
                stringResource(R.string.summary_next_service)
            }
            // IntrinsicSize.Max + fillMaxHeight keeps both summary cards the same height.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .padding(top = SectionGap),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard.Filled(
                    label = stringResource(R.string.summary_fuel_month),
                    value = fmt.money(cards.sumOf { it.fuelCostThisMonth }),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                SummaryCard.Outlined(
                    label = nextServiceLabel,
                    value = reminderTargetText(urgentCard),
                    modifier = Modifier.weight(1f).fillMaxHeight()
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
private fun DashboardHeader(
    userName: String,
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val fmt = LocalAppFormatter.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = fmt.dayDate(System.currentTimeMillis()),
                style = MaterialTheme.typography.labelMedium,
                color = autoBookColors.textSecondary
            )
            Text(
                text = if (userName.isBlank()) stringResource(R.string.dashboard_greeting)
                else stringResource(R.string.greeting_named, userName),
                style = MaterialTheme.typography.headlineLarge,
                color = autoBookColors.textPrimary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderIconButton(
                icon = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.cd_settings),
                onClick = onOpenSettings
            )
            HeaderIconButton(
                icon = Icons.Outlined.Notifications,
                contentDescription = stringResource(R.string.cd_notifications),
                onClick = onOpenNotifications
            )
        }
    }
}

/** Rounded icon affordance used in the dashboard header (settings, notifications). */
@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun DashboardVehicleRow(card: DashboardVehicleCard, modifier: Modifier = Modifier) {
    val v = card.vehicle
    val fmt = LocalAppFormatter.current
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
                        text = fmt.odometer(v.currentOdometer),
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

/** The most-urgent reminder card across all vehicles (worst status, then soonest target). */
private fun mostUrgentCard(cards: List<DashboardVehicleCard>): DashboardVehicleCard? =
    cards.filter { it.reminder != null && it.reminderStatus != null }
        .minWithOrNull(
            compareBy(
                { card -> severityRank(card.reminderStatus!!) },
                { card -> card.reminder!!.nextDate ?: Long.MAX_VALUE },
                { card -> card.reminder!!.nextKm ?: Int.MAX_VALUE }
            )
        )

private fun severityRank(status: ReminderStatus): Int = when (status) {
    ReminderStatus.OVERDUE -> 0
    ReminderStatus.DUE_SOON -> 1
    ReminderStatus.ON_TRACK -> 2
}

/** Target text for the dashboard "next service" card: a date for date-based reminders, else km. */
@Composable
private fun reminderTargetText(card: DashboardVehicleCard?): String {
    val fmt = LocalAppFormatter.current
    val reminder = card?.reminder ?: return stringResource(R.string.not_set)
    return when {
        reminder.remindBy == "date" && reminder.nextDate != null -> fmt.date(reminder.nextDate)
        reminder.nextKm != null -> stringResource(
            R.string.km_remaining,
            fmt.odometer((reminder.nextKm - card.vehicle.currentOdometer).coerceAtLeast(0))
        )
        reminder.nextDate != null -> fmt.date(reminder.nextDate)
        else -> stringResource(R.string.not_set)
    }
}
