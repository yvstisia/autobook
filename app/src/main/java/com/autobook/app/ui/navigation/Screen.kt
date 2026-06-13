package com.autobook.app.ui.navigation

/**
 * All navigation routes in the app. Parameterized routes expose a [createRoute]
 * helper to build a concrete path, and keep their argument name as a constant.
 */
sealed class Screen(val route: String) {
    // Bottom-nav destinations
    data object Dashboard : Screen("dashboard")
    data object VehicleList : Screen("vehicles")
    data object ServiceList : Screen("services")
    data object FuelList : Screen("fuel")
    data object WorkshopList : Screen("workshops")

    // Vehicle sub-screens
    data object AddVehicle : Screen("add_vehicle")
    data object EditVehicle : Screen("edit_vehicle/{vehicleId}") {
        const val ARG_VEHICLE_ID = "vehicleId"
        fun createRoute(vehicleId: Int) = "edit_vehicle/$vehicleId"
    }

    // Service / Fuel add screens (vehicle pre-selected)
    data object AddService : Screen("add_service/{vehicleId}") {
        const val ARG_VEHICLE_ID = "vehicleId"
        fun createRoute(vehicleId: Int) = "add_service/$vehicleId"
    }
    data object AddFuel : Screen("add_fuel/{vehicleId}") {
        const val ARG_VEHICLE_ID = "vehicleId"
        fun createRoute(vehicleId: Int) = "add_fuel/$vehicleId"
    }

    // Workshop sub-screens
    data object AddWorkshop : Screen("add_workshop")
    data object EditWorkshop : Screen("edit_workshop/{workshopId}") {
        const val ARG_WORKSHOP_ID = "workshopId"
        fun createRoute(workshopId: Int) = "edit_workshop/$workshopId"
    }
}
