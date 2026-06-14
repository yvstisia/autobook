package com.autobook.app.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
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
import com.autobook.app.data.local.entity.FuelLog
import com.autobook.app.ui.component.ConfirmDeleteDialog
import com.autobook.app.ui.component.DatePickerField
import com.autobook.app.ui.component.FormScreen
import com.autobook.app.ui.component.FormTextField
import com.autobook.app.ui.component.SelectionChip
import com.autobook.app.ui.component.SummaryCard
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.util.LocalAppFormatter
import com.autobook.app.util.ODOMETER_MAX_DIGITS
import com.autobook.app.util.fuelTypes
import kotlin.math.roundToInt

/**
 * Shared fuel add/edit form. [initial] = null is the add flow; a non-null log pre-fills the
 * fields for editing. [onSubmit] hands back raw field values; the caller decides whether to
 * insert or update. [onDelete] (edit only) shows a confirmation dialog.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FuelFormScreen(
    @StringRes titleRes: Int,
    vehicleName: String,
    initial: FuelLog?,
    onSubmit: (fillDate: Long, liters: Float, pricePerLiterMinor: Int, odometerAtFill: Int, fuelType: String) -> Unit,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val fmt = LocalAppFormatter.current

    var fillDate by remember { mutableStateOf(initial?.fillDate ?: System.currentTimeMillis()) }
    var liters by remember { mutableStateOf(initial?.liters?.toString() ?: "") }
    var pricePerLiter by remember { mutableStateOf(initial?.let { fmt.editableAmount(it.pricePerLiter) } ?: "") }
    var odometer by remember { mutableStateOf(initial?.odometerAtFill?.toString() ?: "") }
    var fuelType by remember { mutableStateOf(initial?.fuelType ?: fuelTypes.first()) }

    var submitted by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val litersValue = liters.toFloatOrNull()
    val priceMinor = fmt.parseMoneyToMinorUnits(pricePerLiter)
    val odometerValid = odometer.toIntOrNull() != null
    val litersValid = litersValue != null && litersValue > 0f
    val priceValid = priceMinor != null && priceMinor > 0

    val liveTotal = if (litersValue != null && priceMinor != null) {
        (litersValue * priceMinor).roundToInt()
    } else 0

    FormScreen(
        title = stringResource(titleRes),
        onBack = onBack,
        submitLabel = stringResource(R.string.action_save),
        deleteLabel = if (onDelete != null) stringResource(R.string.action_delete) else null,
        onDelete = if (onDelete != null) ({ showDeleteDialog = true }) else null,
        onSubmit = {
            submitted = true
            if (litersValid && priceValid && odometerValid) {
                onSubmit(fillDate, litersValue!!, priceMinor!!, odometer.toInt(), fuelType)
            }
        }
    ) {
        Text(
            text = vehicleName,
            style = MaterialTheme.typography.titleMedium,
            color = autoBookColors.textPrimary
        )

        DatePickerField(
            label = stringResource(R.string.field_fill_date),
            value = fillDate,
            onValueChange = { fillDate = it },
            allowFuture = false
        )

        FormTextField(
            label = stringResource(R.string.field_liters),
            value = liters,
            onValueChange = { input -> liters = sanitizeDecimal(input) },
            isError = submitted && !litersValid,
            errorText = stringResource(R.string.error_invalid_number),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        FormTextField(
            label = "${stringResource(R.string.field_price_per_liter)} (${fmt.currency.symbol})",
            value = pricePerLiter,
            onValueChange = { input -> pricePerLiter = sanitizeDecimal(input) },
            isError = submitted && !priceValid,
            errorText = stringResource(R.string.error_invalid_number),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        FormTextField(
            label = stringResource(R.string.field_odometer),
            value = odometer,
            onValueChange = { odometer = it.filter(Char::isDigit).take(ODOMETER_MAX_DIGITS) },
            isError = submitted && !odometerValid,
            errorText = stringResource(R.string.error_invalid_number),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Column {
            Text(
                text = stringResource(R.string.field_fuel_type),
                style = MaterialTheme.typography.labelMedium,
                color = autoBookColors.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                fuelTypes.forEach { type ->
                    SelectionChip(
                        selected = fuelType == type,
                        onClick = { fuelType = type },
                        label = type
                    )
                }
            }
        }

        SummaryCard.Outlined(
            label = stringResource(R.string.field_total_cost),
            value = fmt.money(liveTotal),
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDeleteDialog && onDelete != null) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.delete_fuel_title),
            message = stringResource(R.string.delete_fuel_message),
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

/**
 * Keeps digits and a single decimal separator. Indonesian keyboards emit a comma on
 * KeyboardType.Decimal, so ',' is normalized to '.' for parsing.
 */
private fun sanitizeDecimal(input: String): String {
    val filtered = input.replace(',', '.').filter { it.isDigit() || it == '.' }
    val firstDot = filtered.indexOf('.')
    if (firstDot == -1) return filtered
    val intPart = filtered.substring(0, firstDot + 1)
    val rest = filtered.substring(firstDot + 1).replace(".", "")
    return intPart + rest
}
