package com.autobook.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.autobook.app.data.preferences.AppCurrency
import com.autobook.app.data.preferences.ThemeMode
import com.autobook.app.ui.component.AutoBookBottomNav
import com.autobook.app.ui.navigation.AutoBookNavGraph
import com.autobook.app.ui.navigation.Screen
import com.autobook.app.ui.navigation.bottomNavItems
import com.autobook.app.ui.theme.AutoBookTheme
import com.autobook.app.util.AppFormatter
import com.autobook.app.util.LocalAppFormatter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val container = (application as AutoBookApplication).container
            val prefs = container.userPreferencesRepository

            // Combine the three preferences that gate first render into one nullable state;
            // null means "not loaded yet" so we can avoid an onboarding/theme flash.
            val startState by remember {
                combine(prefs.themeMode, prefs.hasOnboarded, prefs.currency) { theme, onboarded, currency ->
                    StartState(theme, onboarded, currency) as StartState?
                }
            }.collectAsStateWithLifecycle(initialValue = null)

            val state = startState
            val darkTheme = when (state?.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                else -> isSystemInDarkTheme()
            }

            AutoBookTheme(darkTheme = darkTheme) {
                if (state == null) {
                    // Brief splash while preferences load.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                } else {
                    val locale = LocalConfiguration.current.locales[0]
                    CompositionLocalProvider(
                        LocalAppFormatter provides AppFormatter(state.currency, locale)
                    ) {
                        AutoBookRoot(
                            container = container,
                            startDestination = if (state.hasOnboarded) Screen.Dashboard.route
                            else Screen.Onboarding.route
                        )
                    }
                }
            }
        }
    }

    private data class StartState(
        val themeMode: ThemeMode,
        val hasOnboarded: Boolean,
        val currency: AppCurrency
    )
}

@Composable
private fun AutoBookRoot(
    container: AppContainer,
    startDestination: String
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Android 13+ runtime notification permission request on first launch.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* result handled silently; notifications are best-effort */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomNav = bottomNavItems.any { it.screen.route == currentRoute }

    // The floating pill nav overlays the content (DESIGN.md §4.6);
    // screens reserve ~96dp bottom content padding for it.
    Box(modifier = Modifier.fillMaxSize()) {
        AutoBookNavGraph(
            navController = navController,
            container = container,
            startDestination = startDestination,
            onShowMessage = { message ->
                scope.launch { snackbarHostState.showSnackbar(message) }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (showBottomNav) {
            AutoBookBottomNav(
                items = bottomNavItems,
                currentRoute = currentRoute,
                onItemClick = { item ->
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (showBottomNav) 96.dp else 16.dp)
        )
    }
}
