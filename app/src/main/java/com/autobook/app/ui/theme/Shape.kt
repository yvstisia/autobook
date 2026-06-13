package com.autobook.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Shape tokens (DESIGN.md §3)
val RadiusCard = RoundedCornerShape(16.dp)
val RadiusButton = RoundedCornerShape(12.dp)
val RadiusChip = RoundedCornerShape(999.dp)
val RadiusIconChip = RoundedCornerShape(14.dp)

// Spacing tokens
val ScreenPaddingH = 20.dp
val CardPadding = 16.dp
val CardGap = 12.dp
val SectionGap = 24.dp

/**
 * Bottom content padding for every scrollable screen so the floating pill
 * nav (64dp + 16dp margin) never covers the last item (DESIGN.md §4.6).
 */
val BottomNavContentPadding = 96.dp
