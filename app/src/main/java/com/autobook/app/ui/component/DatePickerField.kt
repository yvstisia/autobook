package com.autobook.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.autobook.app.R
import com.autobook.app.ui.theme.RadiusButton
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.util.LocalAppFormatter
import java.util.Calendar
import java.util.TimeZone

/**
 * Form-styled date field (label above, surfaceVariant container) that opens a
 * Material3 date picker dialog when tapped. [value] is epoch millis.
 *
 * Set [allowFuture] = false for "when did this happen" fields (service/fill dates) so a
 * user cannot pick a date after today; keep it true for forward-looking dates (reminders).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    allowFuture: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    val fmt = LocalAppFormatter.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = autoBookColors.textSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        // The field itself is disabled (read-only display); a transparent
        // overlay captures clicks to open the dialog.
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = fmt.date(value),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                shape = RadiusButton,
                colors = formFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showDialog = true }
            )
        }
    }

    if (showDialog) {
        val selectableDates = remember(allowFuture) {
            if (allowFuture) object : SelectableDates {} else pastOrPresentSelectableDates()
        }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = value,
            selectableDates = selectableDates
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(onValueChange)
                    showDialog = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

/**
 * Allows only today and earlier. The picker reports each day as its UTC midnight, so we
 * compare against the UTC midnight of the *local* current date to avoid off-by-one near
 * midnight in timezones ahead of UTC (e.g. WIB).
 */
@OptIn(ExperimentalMaterial3Api::class)
private fun pastOrPresentSelectableDates(): SelectableDates {
    val local = Calendar.getInstance()
    val todayUtcMidnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
    val currentYear = local.get(Calendar.YEAR)
    return object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean =
            utcTimeMillis <= todayUtcMidnight

        override fun isSelectableYear(year: Int): Boolean = year <= currentYear
    }
}
