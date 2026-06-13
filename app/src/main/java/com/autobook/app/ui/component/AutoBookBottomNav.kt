package com.autobook.app.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.autobook.app.ui.navigation.BottomNavItem
import com.autobook.app.ui.theme.RadiusChip
import com.autobook.app.ui.theme.autoBookColors

/**
 * The signature floating pill bottom navigation (DESIGN.md §4.6):
 * dark pill, 64dp tall, 16dp margins, 8dp shadow. Active item gets a
 * primary-at-20% pill highlight behind its icon; colors animate over 150ms.
 */
@Composable
fun AutoBookBottomNav(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = autoBookColors
    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .height(64.dp),
        shape = RadiusChip,
        color = colors.navBackground,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val active = currentRoute == item.screen.route
                val contentColor by animateColorAsState(
                    targetValue = if (active) colors.navActive else colors.navInactive,
                    animationSpec = tween(150),
                    label = "navItemColor"
                )
                val highlightColor by animateColorAsState(
                    targetValue = if (active) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    } else {
                        colors.navBackground.copy(alpha = 0f)
                    },
                    animationSpec = tween(150),
                    label = "navItemHighlight"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onItemClick(item) }
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RadiusChip)
                            .background(highlightColor)
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = stringResource(item.labelRes),
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = stringResource(item.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor
                    )
                }
            }
        }
    }
}
