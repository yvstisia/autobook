package com.autobook.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.autobook.app.AppContainer
import com.autobook.app.ui.UiState
import com.autobook.app.ui.screen.AddFuelScreen
import com.autobook.app.ui.screen.AddServiceScreen
import com.autobook.app.ui.screen.AddVehicleScreen
import com.autobook.app.ui.screen.AddWorkshopScreen
import com.autobook.app.ui.screen.DashboardScreen
import com.autobook.app.ui.screen.EditVehicleScreen
import com.autobook.app.ui.screen.EditWorkshopScreen
import com.autobook.app.ui.screen.FuelListScreen
import com.autobook.app.ui.screen.ServiceListScreen
import com.autobook.app.ui.screen.VehicleListScreen
import com.autobook.app.ui.screen.WorkshopListScreen
import com.autobook.app.ui.viewmodel.DashboardViewModel
import com.autobook.app.ui.viewmodel.DashboardViewModelFactory
import com.autobook.app.ui.viewmodel.FuelViewModel
import com.autobook.app.ui.viewmodel.FuelViewModelFactory
import com.autobook.app.ui.viewmodel.ServiceViewModel
import com.autobook.app.ui.viewmodel.ServiceViewModelFactory
import com.autobook.app.ui.viewmodel.VehicleViewModel
import com.autobook.app.ui.viewmodel.VehicleViewModelFactory
import com.autobook.app.ui.viewmodel.WorkshopViewModel
import com.autobook.app.ui.viewmodel.WorkshopViewModelFactory

@Composable
fun AutoBookNavGraph(
    navController: NavHostController,
    container: AppContainer,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Shared vehicle list, used for filter chips and resolving vehicle names.
    val vehicleViewModel: VehicleViewModel =
        viewModel(factory = VehicleViewModelFactory(container.vehicleRepository))
    val vehiclesState by vehicleViewModel.vehiclesState.collectAsStateWithLifecycle()
    val vehicles = (vehiclesState as? UiState.Success)?.data ?: emptyList()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // --- Dashboard ---
        composable(Screen.Dashboard.route) {
            val vm: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(
                    container.vehicleRepository,
                    container.serviceRepository,
                    container.fuelRepository
                )
            )
            DashboardScreen(
                viewModel = vm,
                onAddFirstVehicle = { navController.navigate(Screen.AddVehicle.route) },
                onSeeAllVehicles = {
                    navController.navigate(Screen.VehicleList.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddService = { navController.navigate(Screen.AddService.createRoute(it)) },
                onAddFuel = { navController.navigate(Screen.AddFuel.createRoute(it)) }
            )
        }

        // --- Vehicles ---
        composable(Screen.VehicleList.route) {
            VehicleListScreen(
                viewModel = vehicleViewModel,
                onAddVehicle = { navController.navigate(Screen.AddVehicle.route) },
                onVehicleClick = { navController.navigate(Screen.EditVehicle.createRoute(it)) }
            )
        }
        composable(Screen.AddVehicle.route) {
            AddVehicleScreen(
                viewModel = vehicleViewModel,
                onBack = { navController.popBackStack() },
                onShowMessage = onShowMessage
            )
        }
        composable(
            route = Screen.EditVehicle.route,
            arguments = listOf(navArgument(Screen.EditVehicle.ARG_VEHICLE_ID) { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt(Screen.EditVehicle.ARG_VEHICLE_ID) ?: return@composable
            EditVehicleScreen(
                viewModel = vehicleViewModel,
                vehicleId = id,
                onBack = { navController.popBackStack() },
                onShowMessage = onShowMessage
            )
        }

        // --- Services ---
        composable(Screen.ServiceList.route) {
            val vm: ServiceViewModel = viewModel(factory = ServiceViewModelFactory(container.serviceRepository))
            ServiceListScreen(
                viewModel = vm,
                vehicles = vehicles,
                onAddService = { navController.navigate(Screen.AddService.createRoute(it)) }
            )
        }
        composable(
            route = Screen.AddService.route,
            arguments = listOf(navArgument(Screen.AddService.ARG_VEHICLE_ID) { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt(Screen.AddService.ARG_VEHICLE_ID) ?: return@composable
            val vm: ServiceViewModel = viewModel(factory = ServiceViewModelFactory(container.serviceRepository))
            AddServiceScreen(
                viewModel = vm,
                vehicleId = id,
                vehicleName = vehicles.firstOrNull { it.id == id }?.nickname ?: "",
                onBack = { navController.popBackStack() },
                onShowMessage = onShowMessage
            )
        }

        // --- Fuel ---
        composable(Screen.FuelList.route) {
            val vm: FuelViewModel = viewModel(factory = FuelViewModelFactory(container.fuelRepository))
            FuelListScreen(
                viewModel = vm,
                vehicles = vehicles,
                onAddFuel = { navController.navigate(Screen.AddFuel.createRoute(it)) }
            )
        }
        composable(
            route = Screen.AddFuel.route,
            arguments = listOf(navArgument(Screen.AddFuel.ARG_VEHICLE_ID) { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt(Screen.AddFuel.ARG_VEHICLE_ID) ?: return@composable
            val vm: FuelViewModel = viewModel(factory = FuelViewModelFactory(container.fuelRepository))
            AddFuelScreen(
                viewModel = vm,
                vehicleId = id,
                vehicleName = vehicles.firstOrNull { it.id == id }?.nickname ?: "",
                onBack = { navController.popBackStack() },
                onShowMessage = onShowMessage
            )
        }

        // --- Workshops ---
        composable(Screen.WorkshopList.route) {
            val vm: WorkshopViewModel = viewModel(factory = WorkshopViewModelFactory(container.workshopRepository))
            WorkshopListScreen(
                viewModel = vm,
                onAddWorkshop = { navController.navigate(Screen.AddWorkshop.route) },
                onWorkshopClick = { navController.navigate(Screen.EditWorkshop.createRoute(it)) }
            )
        }
        composable(Screen.AddWorkshop.route) {
            val vm: WorkshopViewModel = viewModel(factory = WorkshopViewModelFactory(container.workshopRepository))
            AddWorkshopScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onShowMessage = onShowMessage
            )
        }
        composable(
            route = Screen.EditWorkshop.route,
            arguments = listOf(navArgument(Screen.EditWorkshop.ARG_WORKSHOP_ID) { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt(Screen.EditWorkshop.ARG_WORKSHOP_ID) ?: return@composable
            val vm: WorkshopViewModel = viewModel(factory = WorkshopViewModelFactory(container.workshopRepository))
            EditWorkshopScreen(
                viewModel = vm,
                workshopId = id,
                onBack = { navController.popBackStack() },
                onShowMessage = onShowMessage
            )
        }
    }
}
