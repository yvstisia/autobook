package com.autobook.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ---------- Light scheme (DESIGN.md §1) ----------
val LightPrimary = Color(0xFF1D6FE8)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFE6EEFC)
val LightOnPrimaryContainer = Color(0xFF0C447C)
val LightBackground = Color(0xFFFAFBFE)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F4FA)
val LightOutline = Color(0xFFE3E8F2)
val LightTextPrimary = Color(0xFF101522)
val LightTextSecondary = Color(0xFF5F6B85)
val LightTextTertiary = Color(0xFF98A2B8)
val LightSuccess = Color(0xFF0F6E56)
val LightSuccessContainer = Color(0xFFE1F5EE)
val LightWarning = Color(0xFF854F0B)
val LightWarningContainer = Color(0xFFFAEEDA)
val LightDanger = Color(0xFFA32D2D)
val LightDangerContainer = Color(0xFFFCEBEB)
val LightNavBackground = Color(0xFF14181F)

// ---------- Dark scheme ----------
val DarkPrimary = Color(0xFF5B97F0)
val DarkOnPrimary = Color(0xFF04203F)
val DarkPrimaryContainer = Color(0xFF163358)
val DarkOnPrimaryContainer = Color(0xFFB5D4F4)
val DarkBackground = Color(0xFF0E1116)
val DarkSurface = Color(0xFF181D26)
val DarkSurfaceVariant = Color(0xFF1F2735)
val DarkOutline = Color(0xFF2A3140)
val DarkTextPrimary = Color(0xFFECF1F8)
val DarkTextSecondary = Color(0xFF8A94A8)
val DarkTextTertiary = Color(0xFF5F6B85)
val DarkSuccess = Color(0xFF4DD0A1)
val DarkSuccessContainer = Color(0xFF16291F)
val DarkWarning = Color(0xFFEF9F27)
val DarkWarningContainer = Color(0xFF2E2414)
val DarkDanger = Color(0xFFF09595)
val DarkDangerContainer = Color(0xFF2E1717)
val DarkNavBackground = Color(0xFF1F2735)

// Shared nav item colors
val NavActive = Color(0xFFFFFFFF)
val NavInactive = Color(0xFF8A94A8)

/**
 * Tokens the Material 3 color scheme has no slots for (DESIGN.md §1).
 * Provided via [LocalAutoBookColors]; read with [autoBookColors] in composables.
 * Status colors must always pair text-on-container from the same family.
 */
@Immutable
data class AutoBookColors(
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val accentMotor: Color,
    val accentMotorContainer: Color,
    val accentMobil: Color,
    val accentMobilContainer: Color,
    val navBackground: Color,
    val navActive: Color,
    val navInactive: Color
)

val LightAutoBookColors = AutoBookColors(
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    success = LightSuccess,
    successContainer = LightSuccessContainer,
    warning = LightWarning,
    warningContainer = LightWarningContainer,
    danger = LightDanger,
    dangerContainer = LightDangerContainer,
    accentMotor = LightSuccess,
    accentMotorContainer = LightSuccessContainer,
    accentMobil = LightPrimary,
    accentMobilContainer = LightPrimaryContainer,
    navBackground = LightNavBackground,
    navActive = NavActive,
    navInactive = NavInactive
)

val DarkAutoBookColors = AutoBookColors(
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    success = DarkSuccess,
    successContainer = DarkSuccessContainer,
    warning = DarkWarning,
    warningContainer = DarkWarningContainer,
    danger = DarkDanger,
    dangerContainer = DarkDangerContainer,
    accentMotor = DarkSuccess,
    accentMotorContainer = DarkSuccessContainer,
    accentMobil = DarkPrimary,
    accentMobilContainer = DarkPrimaryContainer,
    navBackground = DarkNavBackground,
    navActive = NavActive,
    navInactive = NavInactive
)

val LocalAutoBookColors = staticCompositionLocalOf { LightAutoBookColors }
