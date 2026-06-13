package com.autobook.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.autobook.app.R
import com.autobook.app.data.local.entity.Vehicle
import com.autobook.app.ui.theme.RadiusButton
import com.autobook.app.ui.theme.autoBookColors

/**
 * Shared form used by Add and Edit vehicle screens (DESIGN.md §5.6 style).
 * When [initial] is null the form is empty (add mode); otherwise it is
 * pre-populated and, if [onDelete] is provided, a destructive text button
 * shows under the submit button.
 */
@Composable
fun VehicleForm(
    title: String,
    initial: Vehicle?,
    onSave: (Vehicle) -> Unit,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var nickname by remember { mutableStateOf(initial?.nickname ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "motor") }
    var brand by remember { mutableStateOf(initial?.brand ?: "") }
    var model by remember { mutableStateOf(initial?.model ?: "") }
    var year by remember { mutableStateOf(initial?.year?.toString() ?: "") }
    var odometer by remember { mutableStateOf(initial?.currentOdometer?.toString() ?: "") }

    var submitted by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val yearValid = year.toIntOrNull() != null
    val odometerValid = odometer.toIntOrNull() != null

    FormScreen(
        title = title,
        onBack = onBack,
        submitLabel = stringResource(R.string.action_save),
        onSubmit = {
            submitted = true
            val valid = nickname.isNotBlank() && brand.isNotBlank() &&
                model.isNotBlank() && yearValid && odometerValid
            if (valid) {
                onSave(
                    (initial ?: Vehicle(
                        nickname = "", type = "", brand = "", model = "",
                        year = 0, currentOdometer = 0
                    )).copy(
                        nickname = nickname.trim(),
                        type = type,
                        brand = brand.trim(),
                        model = model.trim(),
                        year = year.toInt(),
                        currentOdometer = odometer.toInt()
                    )
                )
            }
        },
        deleteLabel = if (onDelete != null) stringResource(R.string.action_delete) else null,
        onDelete = if (onDelete != null) ({ showDeleteDialog = true }) else null
    ) {
        FormTextField(
            label = stringResource(R.string.field_nickname),
            value = nickname,
            onValueChange = { nickname = it },
            isError = submitted && nickname.isBlank(),
            errorText = stringResource(R.string.error_required)
        )

        Column {
            Text(
                text = stringResource(R.string.field_type),
                style = MaterialTheme.typography.labelMedium,
                color = autoBookColors.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectionChip(
                    selected = type == "motor",
                    onClick = { type = "motor" },
                    label = stringResource(R.string.type_motor)
                )
                SelectionChip(
                    selected = type == "mobil",
                    onClick = { type = "mobil" },
                    label = stringResource(R.string.type_mobil)
                )
            }
        }

        FormTextField(
            label = stringResource(R.string.field_brand),
            value = brand,
            onValueChange = { brand = it },
            isError = submitted && brand.isBlank(),
            errorText = stringResource(R.string.error_required)
        )
        FormTextField(
            label = stringResource(R.string.field_model),
            value = model,
            onValueChange = { model = it },
            isError = submitted && model.isBlank(),
            errorText = stringResource(R.string.error_required)
        )
        FormTextField(
            label = stringResource(R.string.field_year),
            value = year,
            onValueChange = { year = it.filter(Char::isDigit) },
            isError = submitted && !yearValid,
            errorText = stringResource(R.string.error_invalid_number),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        FormTextField(
            label = stringResource(R.string.field_odometer),
            value = odometer,
            onValueChange = { odometer = it.filter(Char::isDigit) },
            isError = submitted && !odometerValid,
            errorText = stringResource(R.string.error_invalid_number),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Photo: placeholder only (no camera logic yet) — TODO in a later phase.
        OutlinedButton(
            onClick = { /* TODO: photo picker / camera */ },
            shape = RadiusButton,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.AddAPhoto, contentDescription = null)
            Text(
                text = stringResource(R.string.photo_todo),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

    if (showDeleteDialog && onDelete != null) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.delete_vehicle_title),
            message = stringResource(R.string.delete_vehicle_message),
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
