package com.autobook.app.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autobook.app.ui.theme.CardPadding
import com.autobook.app.ui.theme.RadiusCard

/**
 * Base container for every card in the app (DESIGN.md §4.1):
 * surface color, 16dp radius, 1dp outline border, 1dp tonal elevation,
 * 16dp inner padding. No drop shadows.
 */
@Composable
fun AutoBookCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = RadiusCard,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = border
        ) {
            Column(modifier = Modifier.padding(CardPadding), content = content)
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RadiusCard,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = border
        ) {
            Column(modifier = Modifier.padding(CardPadding), content = content)
        }
    }
}
