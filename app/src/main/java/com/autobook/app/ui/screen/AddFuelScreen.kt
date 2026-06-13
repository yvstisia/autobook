package com.autobook.app.ui.screen

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
import com.autobook.app.ui.component.DatePickerField
import com.autobook.app.ui.component.FormScreen
import com.autobook.app.ui.component.FormTextField
import com.autobook.app.ui.component.SelectionChip
import com.autobook.app.ui.component.SummaryCard
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.ui.viewmodel.FuelViewModel
import com.autobook.app.util.LocalAppFormatter
import com.autobook.app.util.fuelTypes
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFuelScreen(
    viewModel: FuelViewModel,
    vehicleId: Int,
    vehicleName: String,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    var fillDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var liters by remember { mutableStateOf("") }
    var pricePerLiter by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf(fuelTypes.first()) }

    var submitted by remember { mutableStateOf(false) }

    val fmt = LocalAppFormatter.current
    val litersValue = liters.toFloatOrNull()
    // Price is entered in major units (decimals allowed for 2-decimal currencies) and
    // stored as minor units, matching how all money is persisted.
    val priceMinor = fmt.parseMoneyToMinorUnits(pricePerLiter)
    val odometerValid = odometer.toIntOrNull() != null
    val litersValid = litersValue != null && litersValue > 0f
    val priceValid = priceMinor != null && priceMinor > 0

    val liveTotal = if (litersValue != null && priceMinor != null) {
        (litersValue * priceMinor).roundToInt()
    } else 0

    val savedMsg = stringResource(R.string.fuel_saved)

    FormScreen(
        title = stringResource(R.string.add_fuel_title),
        onBack = onBack,
        submitLabel = stringResource(R.string.action_save),
        onSubmit = {
            submitted = true
            if (litersValid && priceValid && odometerValid) {
                viewModel.insertFuelLog(
                    vehicleId = vehicleId,
                    fillDate = fillDate,
                    liters = litersValue!!,
                    pricePerLiter = priceMinor!!,
                    odometerAtFill = odometer.toInt(),
                    fuelType = fuelType
                ) {
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
            label = stringResource(R.string.field_fill_date),
            value = fillDate,
            onValueChange = { fillDate = it }
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
            onValueChange = { odometer = it.filter(Char::isDigit) },
            isError = submitted && !odometerValid,
            errorText = stringResource(R.string.error_invalid_number),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Fuel type as selection chips
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

        // Live total (liters × price) above the submit button (DESIGN.md §5.6)
        SummaryCard.Outlined(
            label = stringResource(R.string.field_total_cost),
            value = fmt.money(liveTotal),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Keeps digits and a single decimal separator. Indonesian keyboards emit a
 * comma on KeyboardType.Decimal, so ',' is normalized to '.' for parsing.
 */
private fun sanitizeDecimal(input: String): String {
    val filtered = input.replace(',', '.').filter { it.isDigit() || it == '.' }
    val firstDot = filtered.indexOf('.')
    if (firstDot == -1) return filtered
    val intPart = filtered.substring(0, firstDot + 1)
    val rest = filtered.substring(firstDot + 1).replace(".", "")
    return intPart + rest
}
