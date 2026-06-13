package com.autobook.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Store
import androidx.compose.ui.graphics.vector.ImageVector
import com.autobook.app.R

/** A tab in the bottom navigation bar. */
data class BottomNavItem(
    val screen: Screen,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)

/** The five primary tabs, in display order. */
val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, R.string.nav_dashboard, Icons.Outlined.Home),
    BottomNavItem(Screen.VehicleList, R.string.nav_vehicles, Icons.Outlined.DirectionsCar),
    BottomNavItem(Screen.ServiceList, R.string.nav_services, Icons.Outlined.Build),
    BottomNavItem(Screen.FuelList, R.string.nav_fuel, Icons.Outlined.LocalGasStation),
    BottomNavItem(Screen.WorkshopList, R.string.nav_workshops, Icons.Outlined.Store)
)
