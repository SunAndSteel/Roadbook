package com.florent.carnetconduite

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.florent.carnetconduite.ui.DrivingViewModel
import com.florent.carnetconduite.ui.UiEvent
import com.florent.carnetconduite.ui.screens.HistoryScreen
import com.florent.carnetconduite.ui.screens.HomeScreen
import com.florent.carnetconduite.ui.theme.CarnetConduiteTheme
import com.florent.carnetconduite.ui.theme.ThemeMode
import com.florent.carnetconduite.ui.theme.ThemePreferences
import com.florent.carnetconduite.ui.theme.themeIcon
import kotlinx.coroutines.launch

// Destinations de navigation
sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Accueil", Icons.Default.Home)
    object History : Screen("history", "Historique", Icons.Default.List)
}

val navigationItems = listOf(Screen.Home, Screen.History)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = this
            val themeMode by ThemePreferences.getThemeMode(context)
                .collectAsState(initial = ThemeMode.Dynamic)

            CarnetConduiteTheme(themeMode = themeMode) {
                DrivingLogApp(
                    themeMode = themeMode,
                    onChangeTheme = {
                        val next = when (themeMode) {
                            ThemeMode.Dynamic -> ThemeMode.Light
                            ThemeMode.Light -> ThemeMode.Dark
                            ThemeMode.Dark -> ThemeMode.Dynamic
                        }
                        lifecycleScope.launch {
                            ThemePreferences.saveThemeMode(context, next)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrivingLogApp(
    viewModel: DrivingViewModel = viewModel(),
    themeMode: ThemeMode,
    onChangeTheme: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf<UiEvent.ShowConfirmDialog?>(null) }

    // Écouter les événements UI
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is UiEvent.ShowConfirmDialog -> {
                    showConfirmDialog = event
                }
            }
        }
    }

    // Dialog de confirmation
    showConfirmDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text(dialog.title) },
            text = { Text(dialog.message) },
            confirmButton = {
                Button(onClick = {
                    dialog.onConfirm()
                    showConfirmDialog = null
                }) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Roadbook") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onChangeTheme) {
                        Icon(
                            imageVector = themeIcon(themeMode),
                            contentDescription = "Changer le thème"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navigationItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop jusqu'au start destination
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Éviter plusieurs copies de la même destination
                                launchSingleTop = true
                                // Restaurer l'état lors de la re-sélection
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen(viewModel)
            }
        }
    }
}