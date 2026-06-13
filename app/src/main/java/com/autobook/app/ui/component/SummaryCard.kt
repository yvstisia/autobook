package com.autobook.app.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autobook.app.ui.theme.CardPadding
import com.autobook.app.ui.theme.RadiusCard
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.ui.theme.displayNumber

/**
 * Summary cards (DESIGN.md §4.4). [supportingText] is an optional extra line
 * under the value (e.g. average km/L on the Bensin hero card).
 */
object SummaryCard {

    @Composable
    fun Filled(
        label: String,
        value: String,
        modifier: Modifier = Modifier,
        supportingText: String? = null
    ) {
        Surface(
            modifier = modifier,
            shape = RadiusCard,
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(modifier = Modifier.padding(CardPadding)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = displayNumber,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .animateContentSize()
                )
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun Outlined(
        label: String,
        value: String,
        modifier: Modifier = Modifier,
        supportingText: String? = null
    ) {
        Surface(
            modifier = modifier,
            shape = RadiusCard,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(CardPadding)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = autoBookColors.textSecondary
                )
                Text(
                    text = value,
                    style = displayNumber,
                    color = autoBookColors.textPrimary,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .animateContentSize()
                )
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.labelMedium,
                        color = autoBookColors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
