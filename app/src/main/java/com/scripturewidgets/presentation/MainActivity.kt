// presentation/MainActivity.kt
// Single Activity host for Compose navigation

package com.scripturewidgets.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.scripturewidgets.data.PreferencesRepository
import com.scripturewidgets.presentation.screens.browse.BrowseScreen
import com.scripturewidgets.presentation.screens.favorites.FavoritesScreen
import com.scripturewidgets.presentation.screens.onboarding.OnboardingScreen
import com.scripturewidgets.presentation.screens.settings.SettingsScreen
import com.scripturewidgets.presentation.screens.today.TodayScreen

import com.scripturewidgets.presentation.theme.ScriptureWidgetsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferences: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check onboarding state synchronously for first frame
        val hasSeenOnboarding = runBlocking { preferences.hasSeenOnboarding.first() }

        setContent {
            ScriptureWidgetsTheme {
                ScriptureNavHost(startDestination = if (hasSeenOnboarding) "today" else "onboarding")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// MARK: Navigation Host
// ─────────────────────────────────────────────────────────────────

sealed class Screen(val route: String) {
    data object Today     : Screen("today")
    data object Browse    : Screen("browse")
    data object Favorites : Screen("favorites")
    data object Settings  : Screen("settings")
    data object Onboarding : Screen("onboarding")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Today,     "Today",     Icons.Filled.WbSunny,   Icons.Outlined.WbSunny),
    BottomNavItem(Screen.Browse,    "Browse",    Icons.Filled.MenuBook,  Icons.Outlined.MenuBook),
    BottomNavItem(Screen.Favorites, "Favorites", Icons.Filled.Favorite,  Icons.Outlined.FavoriteBorder),
    BottomNavItem(Screen.Settings,  "Settings",  Icons.Filled.Settings,  Icons.Outlined.Settings)
)

@Composable
fun ScriptureNavHost(startDestination: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route != Screen.Onboarding.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() + slideInHorizontally() },
            exitTransition = { fadeOut() + slideOutHorizontally() }
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Today.route)     { TodayScreen() }
            composable(Screen.Browse.route)    { BrowseScreen() }
            composable(Screen.Favorites.route) { FavoritesScreen() }
            composable(Screen.Settings.route)  { SettingsScreen() }
        }
    }
}
