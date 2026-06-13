package com.autobook.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.autobook.app.ui.theme.RadiusIconChip
import com.autobook.app.ui.theme.autoBookColors

/**
 * Rounded-square vehicle type chip (DESIGN.md §4.2):
 * motor → TwoWheeler in accentMotor, mobil → DirectionsCar in accentMobil.
 */
@Composable
fun VehicleIconChip(type: String, size: Dp = 46.dp, modifier: Modifier = Modifier) {
    val colors = autoBookColors
    val isMobil = type.equals("mobil", ignoreCase = true)
    val container = if (isMobil) colors.accentMobilContainer else colors.accentMotorContainer
    val content = if (isMobil) colors.accentMobil else colors.accentMotor
    val icon = if (isMobil) Icons.Outlined.DirectionsCar else Icons.Outlined.TwoWheeler

    Box(
        modifier = modifier
            .size(size)
            .clip(RadiusIconChip)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(size / 2)
        )
    }
}
