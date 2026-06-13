package com.autobook.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.autobook.app.R
import com.autobook.app.ui.theme.autoBookColors

/**
 * Row of 1..5 stars (DESIGN.md §5.5): filled = statusWarning amber,
 * empty = outline color. Read-only display unless [onRatingChange] is set.
 */
@Composable
fun StarRating(
    rating: Int,
    modifier: Modifier = Modifier,
    starSize: Int = 16,
    onRatingChange: ((Int) -> Unit)? = null
) {
    Row(modifier = modifier) {
        for (star in 1..5) {
            val filled = star <= rating
            val starModifier = if (onRatingChange != null) {
                Modifier.clickable { onRatingChange(star) }
            } else Modifier
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = stringResource(R.string.rating_star, star),
                tint = if (filled) autoBookColors.warning else MaterialTheme.colorScheme.outline,
                modifier = starModifier.size(starSize.dp)
            )
        }
    }
}
