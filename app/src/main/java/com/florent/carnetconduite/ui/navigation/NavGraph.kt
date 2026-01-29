package com.florent.carnetconduite.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.florent.carnetconduite.ui.home.HomeScreen
import com.florent.carnetconduite.ui.history.HistoryScreen
import com.florent.carnetconduite.ui.settings.SettingsScreenContainer
import com.florent.carnetconduite.ui.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Destinations de navigation
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : Screen(
        route = "home",
        title = "Accueil",
        icon = Icons.Default.Home
    )

    object History : Screen(
        route = "history",
        title = "Historique",
        icon = Icons.Default.History
    )

    object Settings : Screen(
        route = "settings",
        title = "Paramètres",
        icon = Icons.Default.Settings
    )
}

/**
 * Graph de navigation refactoré - ViewModels injectés automatiquement
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }

        composable(Screen.History.route) {
            HistoryScreen()
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = koinViewModel()

            SettingsScreenContainer(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}