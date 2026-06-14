package com.autobook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.autobook.app.data.local.entity.ServiceRecord
import com.autobook.app.data.local.entity.ServiceReminder
import com.autobook.app.data.local.entity.Vehicle
import com.autobook.app.ui.component.AutoBookCard
import com.autobook.app.ui.component.AutoBookFab
import com.autobook.app.ui.component.EmptyState
import com.autobook.app.ui.component.StatusBadge
import com.autobook.app.ui.component.VehicleFilterChips
import com.autobook.app.ui.theme.BottomNavContentPadding
import com.autobook.app.ui.theme.CardGap
import com.autobook.app.ui.theme.RadiusChip
import com.autobook.app.ui.theme.ScreenPaddingH
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.ui.theme.numberMedium
import com.autobook.app.ui.viewmodel.ServiceViewModel
import com.autobook.app.util.LocalAppFormatter
import com.autobook.app.util.ReminderStatus
import com.autobook.app.util.computeReminderStatus
import com.autobook.app.util.serviceTypeOptions

@Composable
fun ServiceListScreen(
    viewModel: ServiceViewModel,
    vehicles: List<Vehicle>,
    onAddService: (Int) -> Unit,
    onEditService: (Int) -> Unit
) {
    val selectedId by viewModel.selectedVehicleId.collectAsStateWithLifecycle()
    val records by viewModel.serviceRecords.collectAsStateWithLifecycle()
    val reminders by viewModel.activeReminders.collectAsStateWithLifecycle()

    // Default to first vehicle when nothing is selected yet.
    LaunchedEffect(vehicles, selectedId) {
        if (selectedId == null && vehicles.isNotEmpty()) {
            viewModel.setSelectedVehicle(vehicles.first().id)
        }
    }

    val selectedVehicle = vehicles.firstOrNull { it.id == selectedId }

    Scaffold(
        floatingActionButton = {
            if (selectedVehicle != null) {
                AutoBookFab(onClick = { onAddService(selectedVehicle.id) })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (vehicles.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Build,
                    title = stringResource(R.string.service_empty_title),
                    subtitle = stringResource(R.string.no_vehicle_to_service)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.service_list_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = autoBookColors.textPrimary,
                        modifier = Modifier.padding(start = ScreenPaddingH, top = 20.dp, bottom = 8.dp)
                    )
                    VehicleFilterChips(
                        vehicles = vehicles,
                        selectedVehicleId = selectedId,
                        onSelect = viewModel::setSelectedVehicle
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = ScreenPaddingH,
                            end = ScreenPaddingH,
                            top = 8.dp,
                            bottom = BottomNavContentPadding
                        )
                    ) {
                        // Reminder carousel is always shown (placeholder when there are none),
                        // with arrows to flip between multiple reminders.
                        item {
                            val sorted = if (selectedVehicle != null) {
                                reminders.sortedWith(reminderUrgencyComparator(selectedVehicle.currentOdometer))
                            } else {
                                emptyList()
                            }
                            ReminderCarousel(
                                reminders = sorted,
                                currentOdometer = selectedVehicle?.currentOdometer ?: 0,
                                modifier = Modifier.padding(bottom = CardGap)
                            )
                        }
                        if (records.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.service_empty_subtitle),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = autoBookColors.textTertiary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        } else {
                            items(records, key = { "rec-${it.id}" }) { record ->
                                ServiceCard(
                                    record = record,
                                    onClick = { onEditService(record.id) },
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

/**
 * Always-present reminder section: shows one reminder at a time with left/right arrows to
 * page through them (and a counter), or a placeholder card when there are none.
 */
@Composable
private fun ReminderCarousel(
    reminders: List<ServiceReminder>,
    currentOdometer: Int,
    modifier: Modifier = Modifier
) {
    val count = reminders.size
    // Reset the index when the number of reminders changes so it never goes out of range.
    var index by remember(count) { mutableStateOf(0) }
    val safeIndex = if (count == 0) 0 else index.coerceIn(0, count - 1)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.reminder_section_title),
                style = MaterialTheme.typography.labelMedium,
                color = autoBookColors.textSecondary
            )
            if (count > 1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { index = (safeIndex - 1 + count) % count }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = autoBookColors.textSecondary
                        )
                    }
                    Text(
                        text = "${safeIndex + 1}/$count",
                        style = MaterialTheme.typography.labelMedium,
                        color = autoBookColors.textSecondary
                    )
                    IconButton(onClick = { index = (safeIndex + 1) % count }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = autoBookColors.textSecondary
                        )
                    }
                }
            }
        }

        if (count == 0) {
            AutoBookCard {
                Text(
                    text = stringResource(R.string.reminder_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = autoBookColors.textTertiary
                )
            }
        } else {
            ReminderCard(reminder = reminders[safeIndex], currentOdometer = currentOdometer)
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: ServiceReminder,
    currentOdometer: Int,
    modifier: Modifier = Modifier
) {
    val colors = autoBookColors
    val fmt = LocalAppFormatter.current
    val status = computeReminderStatus(currentOdometer, reminder.nextKm, reminder.nextDate)
    val accentColor = when (status) {
        ReminderStatus.ON_TRACK -> colors.success
        ReminderStatus.DUE_SOON -> colors.warning
        ReminderStatus.OVERDUE -> colors.danger
    }
    val target = reminder.nextKm?.let { fmt.odometer(it) + " " + stringResource(R.string.unit_km) }
        ?: reminder.nextDate?.let { fmt.date(it) }
        ?: stringResource(R.string.not_set)
    // Show the specific service type ("Ganti Oli") for auto reminders; fall back to the
    // generic "next service" label for manual ones with no type.
    val typeLabelRes = serviceTypeLabels[reminder.serviceType]
    val cardTitle = if (typeLabelRes != null) stringResource(typeLabelRes)
    else stringResource(R.string.summary_next_service)

    AutoBookCard(modifier = modifier) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // 3dp status accent bar (DESIGN.md §5.3)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = cardTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
                Text(
                    text = target,
                    style = numberMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            StatusBadge(status = status)
        }
    }
}

// Static lookup, hoisted so it is not rebuilt per card per recomposition.
private val serviceTypeLabels = serviceTypeOptions.associate { it.code to it.labelRes }

/** Orders reminders worst-status-first (overdue > due soon > on track), then soonest target. */
private fun reminderUrgencyComparator(currentOdometer: Int): Comparator<ServiceReminder> =
    compareBy(
        { rem: ServiceReminder ->
            when (computeReminderStatus(currentOdometer, rem.nextKm, rem.nextDate)) {
                ReminderStatus.OVERDUE -> 0
                ReminderStatus.DUE_SOON -> 1
                ReminderStatus.ON_TRACK -> 2
            }
        },
        { rem: ServiceReminder -> rem.nextDate ?: Long.MAX_VALUE },
        { rem: ServiceReminder -> rem.nextKm ?: Int.MAX_VALUE }
    )

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceCard(record: ServiceRecord, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = autoBookColors
    val fmt = LocalAppFormatter.current
    val codes = record.serviceTypes.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    AutoBookCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fmt.date(record.serviceDate),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            if (record.cost > 0) {
                Text(
                    text = fmt.money(record.cost),
                    style = numberMedium,
                    color = colors.textPrimary
                )
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            codes.forEach { code ->
                val labelRes = serviceTypeLabels[code]
                Text(
                    text = if (labelRes != null) stringResource(labelRes) else code,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    modifier = Modifier
                        .clip(RadiusChip)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.service_at_odometer, fmt.odometer(record.odometerAtService)),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textTertiary,
            modifier = Modifier.padding(top = 8.dp)
        )
        record.notes?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
