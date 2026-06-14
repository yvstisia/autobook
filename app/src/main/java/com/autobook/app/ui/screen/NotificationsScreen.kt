package com.autobook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobook.app.R
import com.autobook.app.ui.component.AutoBookCard
import com.autobook.app.ui.component.EmptyState
import com.autobook.app.ui.component.StatusBadge
import com.autobook.app.ui.theme.CardGap
import com.autobook.app.ui.theme.RadiusIconChip
import com.autobook.app.ui.theme.ScreenPaddingH
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.ui.viewmodel.NotificationItem
import com.autobook.app.ui.viewmodel.NotificationsViewModel
import com.autobook.app.util.ReminderStatus
import com.autobook.app.util.serviceTypeOptions

private val serviceTypeLabels = serviceTypeOptions.associate { it.code to it.labelRes }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.notifications.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.notifications_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (items.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Notifications,
                    title = stringResource(R.string.notifications_empty),
                    subtitle = stringResource(R.string.notifications_empty_subtitle)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(ScreenPaddingH)
                ) {
                    items(items, key = { "notif-${it.reminder.id}" }) { item ->
                        NotificationCard(item = item, modifier = Modifier.padding(bottom = CardGap))
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(item: NotificationItem, modifier: Modifier = Modifier) {
    val colors = autoBookColors
    val typeRes = serviceTypeLabels[item.serviceType]
    val typeLabel = if (typeRes != null) stringResource(typeRes)
    else stringResource(R.string.summary_next_service)
    val message = when (item.status) {
        ReminderStatus.OVERDUE -> stringResource(R.string.status_overdue)
        ReminderStatus.DUE_SOON -> stringResource(R.string.status_due_soon)
        ReminderStatus.ON_TRACK -> stringResource(R.string.status_on_track)
    }

    AutoBookCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RadiusIconChip)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = "${item.vehicleName} · $typeLabel",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
            }
            StatusBadge(status = item.status)
        }
    }
}
