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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.autobook.app.R
import com.autobook.app.data.local.entity.ServiceRecord
import com.autobook.app.data.local.entity.ServiceReminder
import com.autobook.app.ui.component.DatePickerField
import com.autobook.app.ui.component.FormScreen
import com.autobook.app.ui.component.FormTextField
import com.autobook.app.ui.component.SelectionChip
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.ui.viewmodel.ServiceViewModel
import com.autobook.app.util.joinCodes
import com.autobook.app.util.remindByOptions
import com.autobook.app.util.serviceTypeOptions

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddServiceScreen(
    viewModel: ServiceViewModel,
    vehicleId: Int,
    vehicleName: String,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    var serviceDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var odometer by remember { mutableStateOf("") }
    val selectedTypes = remember { mutableStateListOf<String>() }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var reminderOn by remember { mutableStateOf(false) }
    var remindBy by remember { mutableStateOf("km") }
    var nextKm by remember { mutableStateOf("") }
    var nextDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var submitted by remember { mutableStateOf(false) }

    val odometerValid = odometer.toIntOrNull() != null
    val typesValid = selectedTypes.isNotEmpty()
    val nextKmValid = nextKm.toIntOrNull() != null

    val savedMsg = stringResource(R.string.service_saved)

    FormScreen(
        title = stringResource(R.string.add_service_title),
        onBack = onBack,
        submitLabel = stringResource(R.string.action_save),
        onSubmit = {
            submitted = true
            val reminderValid = !reminderOn || ((remindBy == "date") || nextKmValid)
            if (odometerValid && typesValid && reminderValid) {
                val record = ServiceRecord(
                    vehicleId = vehicleId,
                    serviceDate = serviceDate,
                    odometerAtService = odometer.toInt(),
                    serviceTypes = joinCodes(selectedTypes),
                    cost = cost.toIntOrNull() ?: 0,
                    notes = notes.takeIf { it.isNotBlank() }
                )
                val reminderDraft = if (reminderOn) {
                    ServiceReminder(
                        serviceRecordId = 0, // linked in ViewModel after insert
                        vehicleId = vehicleId,
                        remindBy = remindBy,
                        nextKm = if (remindBy == "km" || remindBy == "both") nextKm.toIntOrNull() else null,
                        nextDate = if (remindBy == "date" || remindBy == "both") nextDate else null
                    )
                } else null

                viewModel.saveService(record, reminderDraft) {
                    onShowMessage(savedMsg)
                    onBack()
                }
            }
        }
    ) {
        // Vehicle is pre-selected from the nav argument (non-editable).
        Text(
            text = vehicleName,
            style = MaterialTheme.typography.titleMedium,
            color = autoBookColors.textPrimary
        )

        DatePickerField(
            label = stringResource(R.string.field_service_date),
            value = serviceDate,
            onValueChange = { serviceDate = it }
        )

        FormTextField(
            label = stringResource(R.string.field_odometer),
            value = odometer,
            onValueChange = { odometer = it.filter(Char::isDigit) },
            isError = submitted && !odometerValid,
            errorText = stringResource(R.string.error_invalid_number),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

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
            label = stringResource(R.string.field_cost),
            value = cost,
            onValueChange = { cost = it.filter(Char::isDigit) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        FormTextField(
            label = stringResource(R.string.field_notes),
            value = notes,
            onValueChange = { notes = it },
            singleLine = false
        )

        // Reminder section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.reminder_set),
                style = MaterialTheme.typography.bodyMedium,
                color = autoBookColors.textPrimary
            )
            Switch(checked = reminderOn, onCheckedChange = { reminderOn = it })
        }

        if (reminderOn) {
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

            if (remindBy == "km" || remindBy == "both") {
                FormTextField(
                    label = stringResource(R.string.field_next_km),
                    value = nextKm,
                    onValueChange = { nextKm = it.filter(Char::isDigit) },
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
    }
}
