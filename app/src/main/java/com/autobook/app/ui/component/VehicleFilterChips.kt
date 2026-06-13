package com.autobook.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autobook.app.data.local.entity.Vehicle
import com.autobook.app.ui.theme.ScreenPaddingH

/** Horizontal row of vehicle filter chips (DESIGN.md §5.3), full-rounded style. */
@Composable
fun VehicleFilterChips(
    vehicles: List<Vehicle>,
    selectedVehicleId: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = ScreenPaddingH, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(vehicles, key = { it.id }) { vehicle ->
            SelectionChip(
                selected = vehicle.id == selectedVehicleId,
                onClick = { onSelect(vehicle.id) },
                label = vehicle.nickname
            )
        }
    }
}
