package com.autobook.app.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobook.app.R
import com.autobook.app.data.local.entity.Workshop
import com.autobook.app.ui.component.AutoBookCard
import com.autobook.app.ui.component.AutoBookFab
import com.autobook.app.ui.component.EmptyState
import com.autobook.app.ui.component.StarRating
import com.autobook.app.ui.component.formFieldColors
import com.autobook.app.ui.theme.BottomNavContentPadding
import com.autobook.app.ui.theme.CardGap
import com.autobook.app.ui.theme.RadiusButton
import com.autobook.app.ui.theme.RadiusChip
import com.autobook.app.ui.theme.ScreenPaddingH
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.ui.viewmodel.WorkshopViewModel
import com.autobook.app.util.specializationOptions

@Composable
fun WorkshopListScreen(
    viewModel: WorkshopViewModel,
    onAddWorkshop: () -> Unit,
    onWorkshopClick: (Int) -> Unit
) {
    val workshops by viewModel.workshops.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = { AutoBookFab(onClick = onAddWorkshop) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = stringResource(R.string.workshop_list_title),
                style = MaterialTheme.typography.headlineLarge,
                color = autoBookColors.textPrimary,
                modifier = Modifier.padding(start = ScreenPaddingH, top = 20.dp, bottom = 8.dp)
            )
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setSearchQuery,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_workshop),
                        color = autoBookColors.textTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.cd_search),
                        tint = autoBookColors.textTertiary
                    )
                },
                singleLine = true,
                shape = RadiusButton,
                colors = formFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPaddingH, vertical = 8.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (workshops.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.Store,
                        title = stringResource(R.string.workshop_empty_title),
                        subtitle = stringResource(R.string.workshop_empty_subtitle)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = ScreenPaddingH,
                            end = ScreenPaddingH,
                            top = 8.dp,
                            bottom = BottomNavContentPadding
                        )
                    ) {
                        items(workshops, key = { it.id }) { workshop ->
                            WorkshopCard(
                                workshop = workshop,
                                onClick = { onWorkshopClick(workshop.id) },
                                modifier = Modifier.padding(bottom = CardGap)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Static lookup, hoisted so it is not rebuilt per card per recomposition.
private val specializationLabels = specializationOptions.associate { it.code to it.labelRes }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkshopCard(workshop: Workshop, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = autoBookColors
    val codes = workshop.specialization.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    AutoBookCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = workshop.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            StarRating(rating = workshop.rating)
        }
        workshop.address?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (codes.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                codes.forEach { code ->
                    val labelRes = specializationLabels[code]
                    Text(
                        text = if (labelRes != null) stringResource(labelRes) else code,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        modifier = Modifier
                            .clip(RadiusChip)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
        if (workshop.latitude != null && workshop.longitude != null) {
            TextButton(
                onClick = {
                    val lat = workshop.latitude
                    val lng = workshop.longitude
                    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(workshop.name)})")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.open_in_maps),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}
