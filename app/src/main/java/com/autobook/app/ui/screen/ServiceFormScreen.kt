package com.autobook.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import com.autobook.app.R
import com.autobook.app.data.local.entity.ServiceRecord
import com.autobook.app.data.local.entity.ServiceReminder
import com.autobook.app.ui.component.ConfirmDeleteDialog
import com.autobook.app.ui.component.DatePickerField
import com.autobook.app.ui.component.FormScreen
import com.autobook.app.ui.component.FormTextField
import com.autobook.app.ui.component.SegmentedChips
import com.autobook.app.ui.component.SelectionChip
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.util.AutoReminderCalculator
import com.autobook.app.util.LocalAppFormatter
import com.autobook.app.util.ODOMETER_MAX_DIGITS
import com.autobook.app.util.joinCodes
import com.autobook.app.util.remindByOptions
import com.autobook.app.util.serviceTypeOptions
import com.autobook.app.util.splitCodes

/** Reminder generation mode chosen in the service form. */
private enum class ReminderMode { AUTO, MANUAL, OFF }

/**
 * Shared service add/edit form. [initial] = null is the add flow; a non-null record
 * pre-fills the fields for editing. [onDelete] (edit only) shows a confirmation dialog.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceFormScreen(
    @StringRes titleRes: Int,
    vehicleId: Int,
    vehicleName: String,
    vehicleType: String,
    vehicleCurrentOdometer: Int,
    initial: ServiceRecord?,
    workshopSuggestions: List<String> = emptyList(),
    onSubmit: (ServiceRecord, List<ServiceReminder>) -> Unit,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val fmt = LocalAppFormatter.current

    var serviceDate by remember { mutableStateOf(initial?.serviceDate ?: System.currentTimeMillis()) }
    var odometer by remember { mutableStateOf(initial?.odometerAtService?.toString() ?: "") }
    val selectedTypes = remember {
        mutableStateListOf<String>().apply { initial?.let { addAll(splitCodes(it.serviceTypes)) } }
    }
    var cost by remember { mutableStateOf(initial?.let { fmt.editableAmount(it.cost) } ?: "") }
    var workshopName by remember { mutableStateOf(initial?.workshopName ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    var reminderMode by remember { mutableStateOf(ReminderMode.AUTO) }
    var remindBy by remember { mutableStateOf("both") }
    var nextKm by remember { mutableStateOf("") }
    var nextDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var submitted by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val odometerValid = odometer.toIntOrNull() != null
    val odometerLow = odometer.toIntOrNull()?.let { it < vehicleCurrentOdometer } ?: false
    val typesValid = selectedTypes.isNotEmpty()
    val nextKmValid = nextKm.toIntOrNull() != null
    val reminderAvailable = selectedTypes.any { it in AutoReminderCalculator.PREDICTABLE_TYPES }

    FormScreen(
        title = stringResource(titleRes),
        onBack = onBack,
        submitLabel = stringResource(R.string.action_save),
        deleteLabel = if (onDelete != null) stringResource(R.string.action_delete) else null,
        onDelete = if (onDelete != null) ({ showDeleteDialog = true }) else null,
        onSubmit = {
            submitted = true
            val reminderValid = !reminderAvailable || reminderMode != ReminderMode.MANUAL ||
                (remindBy == "date") || nextKmValid
            if (odometerValid && typesValid && reminderValid) {
                val record = ServiceRecord(
                    id = initial?.id ?: 0,
                    vehicleId = vehicleId,
                    serviceDate = serviceDate,
                    odometerAtService = odometer.toInt(),
                    serviceTypes = joinCodes(selectedTypes),
                    cost = fmt.parseMoneyToMinorUnits(cost) ?: 0,
                    notes = notes.takeIf { it.isNotBlank() },
                    workshopName = workshopName.takeIf { it.isNotBlank() }?.trim()
                )
                val reminders = if (!reminderAvailable) emptyList() else when (reminderMode) {
                    ReminderMode.AUTO -> AutoReminderCalculator.generate(
                        vehicleType = vehicleType,
                        vehicleId = vehicleId,
                        serviceDate = serviceDate,
                        odometerAtService = odometer.toInt(),
                        selectedTypes = selectedTypes,
                        remindBy = remindBy
                    )
                    ReminderMode.MANUAL -> listOf(
                        ServiceReminder(
                            serviceRecordId = 0, // linked in ViewModel
                            vehicleId = vehicleId,
                            remindBy = remindBy,
                            nextKm = if (remindBy == "km" || remindBy == "both") nextKm.toIntOrNull() else null,
                            nextDate = if (remindBy == "date" || remindBy == "both") nextDate else null
                        )
                    )
                    ReminderMode.OFF -> emptyList()
                }
                onSubmit(record, reminders)
            }
        }
    ) {
        // Vehicle is fixed (pre-selected on add, owner of the record on edit).
        Text(
            text = vehicleName,
            style = MaterialTheme.typography.titleMedium,
            color = autoBookColors.textPrimary
        )

        DatePickerField(
            label = stringResource(R.string.field_service_date),
            value = serviceDate,
            onValueChange = { serviceDate = it },
            allowFuture = false
        )

        Column {
            FormTextField(
                label = stringResource(R.string.field_odometer),
                value = odometer,
                onValueChange = { odometer = it.filter(Char::isDigit).take(ODOMETER_MAX_DIGITS) },
                isError = submitted && !odometerValid,
                errorText = stringResource(R.string.error_invalid_number),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (odometerLow) {
                Text(
                    text = stringResource(R.string.odometer_warning_low),
                    style = MaterialTheme.typography.labelMedium,
                    color = autoBookColors.warning,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Column {
            Text(
                text = stringResource(R.string.field_service_types),
                style = MaterialTheme.typography.labelMedium,
                color = autoBookColors.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                serviceTypeOptions.forEach { option ->
                    val selected = option.code in selectedTypes
                    SelectionChip(
                        selected = selected,
                        onClick = {
                            if (selected) selectedTypes.remove(option.code)
                            else selectedTypes.add(option.code)
                        },
                        label = stringResource(option.labelRes)
                    )
                }
            }
            if (submitted && !typesValid) {
                Text(
                    text = stringResource(R.string.error_required),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        FormTextField(
            label = "${stringResource(R.string.field_cost)} (${fmt.currency.symbol})",
            value = cost,
            onValueChange = { input -> cost = input.filter { it.isDigit() || it == '.' || it == ',' } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Column {
            FormTextField(
                label = stringResource(R.string.field_workshop),
                value = workshopName,
                onValueChange = { workshopName = it },
                placeholder = stringResource(R.string.workshop_placeholder)
            )
            // Suggest existing workshops matching what's typed; tap a chip to fill it in.
            val matches = workshopSuggestions.filter {
                workshopName.isNotBlank() &&
                    it.contains(workshopName.trim(), ignoreCase = true) &&
                    !it.equals(workshopName.trim(), ignoreCase = true)
            }.distinct().take(5)
            if (matches.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    matches.forEach { name ->
                        SelectionChip(
                            selected = false,
                            onClick = { workshopName = name },
                            label = name
                        )
                    }
                }
            }
        }

        FormTextField(
            label = stringResource(R.string.field_notes),
            value = notes,
            onValueChange = { notes = it },
            singleLine = false
        )

        // Reminder section: only available when a predictable type (oil/tune-up/battery)
        // is selected. Tires/brakes/other alone cannot be scheduled.
        if (reminderAvailable) {
            Column {
                Text(
                    text = stringResource(R.string.reminder_mode),
                    style = MaterialTheme.typography.labelMedium,
                    color = autoBookColors.textSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                SegmentedChips(
                    options = listOf(
                        ReminderMode.AUTO to stringResource(R.string.reminder_mode_auto),
                        ReminderMode.MANUAL to stringResource(R.string.reminder_mode_manual),
                        ReminderMode.OFF to stringResource(R.string.reminder_mode_off)
                    ),
                    selected = reminderMode,
                    onSelect = { reminderMode = it }
                )
            }

            // Remind-by basis applies to both Automatic (preference for generated reminders)
            // and Manual (the custom reminder's basis).
            if (reminderMode != ReminderMode.OFF) {
                Column {
                    Text(
                        text = stringResource(R.string.reminder_remind_by),
                        style = MaterialTheme.typography.labelMedium,
                        color = autoBookColors.textSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        remindByOptions.forEach { option ->
                            SelectionChip(
                                selected = remindBy == option.code,
                                onClick = { remindBy = option.code },
                                label = stringResource(option.labelRes)
                            )
                        }
                    }
                }
            }

            when (reminderMode) {
                ReminderMode.AUTO -> Text(
                    text = stringResource(R.string.reminder_auto_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = autoBookColors.textTertiary
                )

                ReminderMode.MANUAL -> {
                    if (remindBy == "km" || remindBy == "both") {
                        FormTextField(
                            label = stringResource(R.string.field_next_km),
                            value = nextKm,
                            onValueChange = { nextKm = it.filter(Char::isDigit).take(ODOMETER_MAX_DIGITS) },
                            isError = submitted && !nextKmValid,
                            errorText = stringResource(R.string.error_invalid_number),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    if (remindBy == "date" || remindBy == "both") {
                        DatePickerField(
                            label = stringResource(R.string.field_next_date),
                            value = nextDate,
                            onValueChange = { nextDate = it }
                        )
                    }
                }

                ReminderMode.OFF -> { /* no reminders */ }
            }
        }
    }

    if (showDeleteDialog && onDelete != null) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.delete_service_title),
            message = stringResource(R.string.delete_service_message),
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
