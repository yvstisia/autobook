package com.autobook.app.ui.component

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.autobook.app.R
import com.autobook.app.data.local.entity.Workshop
import com.autobook.app.ui.theme.RadiusButton
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.util.getCurrentLocation
import com.autobook.app.util.joinCodes
import com.autobook.app.util.specializationOptions
import com.autobook.app.util.splitCodes
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Shared form used by Add and Edit workshop screens (DESIGN.md §5.6 style).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkshopForm(
    title: String,
    initial: Workshop?,
    onSave: (Workshop) -> Unit,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var latitude by remember { mutableStateOf(initial?.latitude) }
    var longitude by remember { mutableStateOf(initial?.longitude) }
    var rating by remember { mutableStateOf(initial?.rating ?: 0) }
    val selectedSpecs = remember {
        mutableStateListOf<String>().apply { addAll(splitCodes(initial?.specialization ?: "")) }
    }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    var submitted by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun fetchLocation() {
        scope.launch {
            val loc = getCurrentLocation(context)
            if (loc != null) {
                latitude = loc.latitude
                longitude = loc.longitude
                permissionDenied = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDenied = false
            fetchLocation()
        } else {
            permissionDenied = true
        }
    }

    FormScreen(
        title = title,
        onBack = onBack,
        submitLabel = stringResource(R.string.action_save),
        onSubmit = {
            submitted = true
            if (name.isNotBlank()) {
                onSave(
                    (initial ?: Workshop(name = "", rating = 0, specialization = "")).copy(
                        name = name.trim(),
                        address = address.takeIf { it.isNotBlank() },
                        latitude = latitude,
                        longitude = longitude,
                        rating = rating,
                        specialization = joinCodes(selectedSpecs),
                        notes = notes.takeIf { it.isNotBlank() }
                    )
                )
            }
        },
        deleteLabel = if (onDelete != null) stringResource(R.string.action_delete) else null,
        onDelete = if (onDelete != null) ({ showDeleteDialog = true }) else null
    ) {
        FormTextField(
            label = stringResource(R.string.field_workshop_name),
            value = name,
            onValueChange = { name = it },
            isError = submitted && name.isBlank(),
            errorText = stringResource(R.string.error_required)
        )
        FormTextField(
            label = stringResource(R.string.field_address),
            value = address,
            onValueChange = { address = it },
            singleLine = false
        )

        // GPS tag section
        Column {
            OutlinedButton(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) fetchLocation() else
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                shape = RadiusButton,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.MyLocation, contentDescription = null)
                Text(
                    text = stringResource(R.string.use_current_location),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (latitude != null && longitude != null) {
                Text(
                    text = stringResource(
                        R.string.location_saved,
                        String.format(Locale.US, "%.5f", latitude),
                        String.format(Locale.US, "%.5f", longitude)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = autoBookColors.textSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            if (permissionDenied) {
                Text(
                    text = stringResource(R.string.location_permission_needed),
                    style = MaterialTheme.typography.labelMedium,
                    color = autoBookColors.danger,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // Star rating
        Column {
            Text(
                text = stringResource(R.string.field_rating),
                style = MaterialTheme.typography.labelMedium,
                color = autoBookColors.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            StarRating(rating = rating, starSize = 28, onRatingChange = { rating = it })
        }

        // Specialization chips
        Column {
            Text(
                text = stringResource(R.string.field_specialization),
                style = MaterialTheme.typography.labelMedium,
                color = autoBookColors.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                specializationOptions.forEach { option ->
                    val selected = option.code in selectedSpecs
                    SelectionChip(
                        selected = selected,
                        onClick = {
                            if (selected) selectedSpecs.remove(option.code)
                            else selectedSpecs.add(option.code)
                        },
                        label = stringResource(option.labelRes)
                    )
                }
            }
        }

        FormTextField(
            label = stringResource(R.string.field_notes),
            value = notes,
            onValueChange = { notes = it },
            singleLine = false
        )
    }

    if (showDeleteDialog && onDelete != null) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.delete_workshop_title),
            message = stringResource(R.string.delete_workshop_message),
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
