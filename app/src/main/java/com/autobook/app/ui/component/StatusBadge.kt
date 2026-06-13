package com.autobook.app.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.autobook.app.R
import com.autobook.app.ui.theme.RadiusChip
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.util.ReminderStatus

/**
 * Full-rounded reminder status pill (DESIGN.md §4.3).
 * null status = no reminder/service yet → "Belum ada servis" in warning colors.
 */
@Composable
fun StatusBadge(status: ReminderStatus?, modifier: Modifier = Modifier) {
    val colors = autoBookColors
    val (labelRes, contentColor, containerColor) = when (status) {
        ReminderStatus.ON_TRACK -> Triple(R.string.status_on_track, colors.success, colors.successContainer)
        ReminderStatus.DUE_SOON -> Triple(R.string.status_due_soon, colors.warning, colors.warningContainer)
        ReminderStatus.OVERDUE -> Triple(R.string.status_overdue, colors.danger, colors.dangerContainer)
        null -> Triple(R.string.status_no_service, colors.warning, colors.warningContainer)
    }
    Text(
        text = stringResource(labelRes),
        style = MaterialTheme.typography.labelSmall,
        color = contentColor,
        modifier = modifier
            .clip(RadiusChip)
            .background(containerColor)
            .animateContentSize()
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
