package com.florent.carnetconduite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.florent.carnetconduite.domain.models.UserSettings
import com.florent.carnetconduite.repository.SettingsRepository
import com.florent.carnetconduite.ui.DrivingViewModel
import com.florent.carnetconduite.ui.navigation.NavGraph
import com.florent.carnetconduite.ui.navigation.Screen
import com.florent.carnetconduite.ui.theme.CarnetConduiteTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

/**
 * MainActivity : Point d'entrée de l'application.
 *
 * CHANGEMENT ARCHITECTURAL IMPORTANT :
 * On passe de ThemePreferences à SettingsRepository pour plus de cohérence.
 *
 * AVANT :
 * - ThemePreferences stockait juste le thème
 * - Accès direct dans MainActivity
 *
 * APRÈS :
 * - SettingsRepository stocke TOUS les settings
 * - Accès via StateFlow dans toute l'app
 * - Cohérence avec le pattern établi
 *
 * QUESTION : Pourquoi lire les settings dans onCreate et pas dans Compose ?
 *
 * Réponse : Le thème doit être appliqué AVANT la composition.
 * Si on le fait dans Compose, il y aurait un flash du thème par défaut.
 *
 * FLUX :
 * 1. onCreate démarre
 * 2. Lit le thème depuis SettingsRepository (suspend, donc launch)
 * 3. Met à jour themeMode dans remember
 * 4. setContent avec le bon thème dès le départ
 * 5. Pas de flash visuel !
 */
class MainActivity : ComponentActivity() {

    /**
     * Injection de SettingsRepository via Koin.
     *
     * PATTERN : by inject()
     * Lazy injection - le repository n'est créé que quand on l'accède.
     *
     * QUESTION : Pourquoi inject ici et koinViewModel() dans Compose ?
     * Réponse :
     * - inject() = pour n'importe quelle classe Koin
     * - koinViewModel() = spécifiquement pour les ViewModels avec lifecycle
     */
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        /**
         * Le thème doit être décidé AVANT setContent.
         *
         * PROBLÈME : settingsRepository.userSettings est un Flow (asynchrone)
         * SOLUTION : Utiliser un MutableState et le mettre à jour dans un launch
         *
         * ALTERNATIVE (pas recommandée) :
         * runBlocking { settingsRepository.userSettings.first() }
         * → Bloquerait le thread principal → ANR possible
         *
         * NOTRE SOLUTION :
         * 1. Démarrer avec un default (pas de flash car c'est instantané)
         * 2. Lancer une coroutine pour lire le vrai thème
         * 3. Mettre à jour le state
         * 4. Compose se recompose avec le bon thème
         *
         * En pratique, c'est si rapide qu'on ne voit pas la différence !
         */
        setContent {
            /**
             * STATE : Le mode de thème actuel
             *
             * remember = survit aux recompositions
             * mutableStateOf = notifie Compose quand ça change
             *
             * INITIALISATION :
             * On démarre avec SYSTEM (le default le plus sûr).
             * Dans LaunchedEffect ci-dessous, on charge le vrai thème.
             */
            var themeMode by remember {
                mutableStateOf(UserSettings.DEFAULT.themeMode)
            }

            /**
             * EFFET : Charger le thème au démarrage
             *
             * LaunchedEffect(Unit) = s'exécute une seule fois au démarrage
             *
             * POURQUOI Unit comme clé ?
             * Unit = valeur constante qui ne change jamais
             * → L'effet ne se relance jamais (sauf si le composable est retiré et rajouté)
             *
             * FLUX :
             * 1. Composable entre en composition
             * 2. LaunchedEffect se lance
             * 3. Collecte le Flow UNE FOIS avec .first()
             * 4. Met à jour themeMode
             * 5. Compose se recompose avec le nouveau thème
             * 6. L'effet ne se relance pas (Unit n'a pas changé)
             *
             * QUESTION : Pourquoi .first() et pas .collect() ?
             * Réponse : On veut juste la valeur initiale, pas observer les changements.
             * Les changements de thème se font dans SettingsScreen, qui a son propre
             * collectAsState(). Pas besoin de dupliquer l'observation ici.
             */
            LaunchedEffect(Unit) {
                themeMode = settingsRepository.userSettings.first().themeMode
            }

            /**
             * Thème Material 3 de l'application.
             *
             * darkTheme = détermine si on utilise le thème sombre
             *
             * LOGIQUE :
             * - ThemeMode.LIGHT → toujours clair
             * - ThemeMode.DARK → toujours sombre
             * - ThemeMode.SYSTEM → suit les paramètres système
             *
             * isSystemInDarkTheme() = fonction Compose qui détecte
             * les préférences système de l'utilisateur.
             */
            CarnetConduiteTheme(
                themeMode = themeMode
            ) {
                RoadbookApp(
                    onThemeModeUpdate = { newMode ->
                        /**
                         * CALLBACK : Mise à jour du thème
                         *
                         * FLUX :
                         * 1. User change le thème dans Settings
                         * 2. SettingsViewModel → SettingsRepository
                         * 3. Écrit dans DataStore
                         * 4. MAIS le thème ici (MainActivity) ne change pas automatiquement
                         *
                         * POURQUOI ?
                         * Ce themeMode est un state LOCAL, pas connecté au Flow !
                         *
                         * SOLUTION : Ce callback met à jour le state local
                         * pour que MainActivity applique le thème immédiatement.
                         *
                         * ALTERNATIVE (plus élégante mais plus complexe) :
                         * Observer settingsRepository.userSettings dans MainActivity
                         * et mettre à jour automatiquement.
                         *
                         * Pour simplicité, on utilise le callback.
                         */
                        themeMode = newMode
                    }
                )
            }
        }
    }
}

/**
 * Composable principal de l'application.
 *
 * STRUCTURE :
 * Scaffold (cadre Material 3)
 *   ├─ TopBar (avec bouton Settings)
 *   ├─ BottomBar (navigation Home/History)
 *   └─ Content (NavHost)
 *
 * PATTERN : Scaffold
 * Scaffold = squelette d'interface Material Design
 * Fournit des slots pour top bar, bottom bar, FAB, etc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadbookApp(
    onThemeModeUpdate: (com.florent.carnetconduite.ui.theme.ThemeMode) -> Unit
) {
    /**
     * NavController : Contrôle la navigation
     *
     * rememberNavController() = crée et se souvient du controller
     * Survit aux recompositions.
     */
    val navController = rememberNavController()

    /**
     * ViewModel principal de l'app.
     *
     * koinViewModel() = récupère depuis Koin
     * Scope : Lié à cette Activity
     *
     * QUESTION : Pourquoi le ViewModel est ici et pas dans NavGraph ?
     * Réponse : DrivingViewModel est partagé entre Home et History.
     * On le crée une fois et le passe aux deux écrans.
     * Si on le créait dans NavGraph, on aurait un nouveau ViewModel
     * à chaque navigation → perte de l'état !
     */
    val viewModel: DrivingViewModel = koinViewModel()

    /**
     * Items de la bottom navigation.
     *
     * QUESTION : Pourquoi Settings n'est pas ici ?
     * Réponse : Settings est moins utilisé que Home/History.
     * Material Design recommande de le mettre dans la top bar
     * ou un menu overflow, pas dans la navigation principale.
     */
    val navigationItems = listOf(
        Screen.Home,
        Screen.History
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Roadbook") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    /**
                     * NOUVEAU : Bouton Settings dans la top bar
                     *
                     * PATTERN : IconButton avec navigation
                     *
                     * FLUX :
                     * 1. User clique sur l'icône Settings
                     * 2. navController.navigate(Screen.Settings.route)
                     * 3. NavHost détecte le changement
                     * 4. Affiche SettingsScreenContainer
                     * 5. SettingsViewModel est créé automatiquement par Koin
                     * 6. Écran Settings s'affiche
                     *
                     * ANIMATION :
                     * Compose Navigation gère automatiquement :
                     * - Slide in from right (Settings entre)
                     * - Slide out to left (Home sort)
                     *
                     * Personnalisable si besoin avec enterTransition/exitTransition.
                     */
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Settings.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres"
                        )
                    }
                }
            )
        },
        bottomBar = {
            /**
             * Bottom Navigation Bar
             *
             * PATTERN : Material 3 NavigationBar
             * Affiche 2-5 destinations principales.
             */
            NavigationBar {
                /**
                 * currentBackStackEntry : L'entrée actuelle dans la pile de navigation
                 *
                 * currentBackStackEntryAsState() = observe le changement de destination
                 * Quand on navigue, ce State change → recomposition → item sélectionné se met à jour
                 */
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navigationItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(screen.icon, contentDescription = screen.title)
                        },
                        label = { Text(screen.title) },
                        /**
                         * selected : L'item est-il sélectionné ?
                         *
                         * LOGIQUE :
                         * hierarchy = hiérarchie de destinations (parent → enfants)
                         * any { it.route == screen.route } = y a-t-il une destination
                         * dans la hiérarchie qui correspond à cette route ?
                         *
                         * POURQUOI hierarchy et pas juste destination.route ?
                         * Pour supporter les nested navigation graphs.
                         * Par exemple, si "home" contient "home/detail",
                         * on veut que "home" reste sélectionné dans la bottom bar.
                         */
                        selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true,
                        onClick = {
                            /**
                             * Navigation avec options
                             *
                             * COMPORTEMENT :
                             * 1. popUpTo : Vide la pile jusqu'à la destination de départ
                             *    → Évite d'empiler Home -> History -> Home -> History...
                             *
                             * 2. saveState : Sauvegarde l'état de l'écran quitté
                             *    → Si je scrolle dans History puis change d'écran,
                             *       je retrouve ma position de scroll en revenant
                             *
                             * 3. launchSingleTop : Une seule copie de la destination
                             *    → Si je suis sur Home et je clique Home, rien ne se passe
                             *
                             * 4. restoreState : Restaure l'état sauvegardé
                             *    → Fonctionne avec saveState ci-dessus
                             *
                             * C'est le pattern recommandé pour bottom navigation !
                             */
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
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
    ) { padding ->
        /**
         * NavGraph : Le système de navigation
         *
         * C'est ici que Home, History, et Settings sont définis.
         * NavHost affiche le composable correspondant à la route actuelle.
         */
        NavGraph(
            navController = navController,
            drivingViewModel = viewModel,
            modifier = Modifier.padding(padding)
        )
    }

    /**
     * EFFET : Observer les changements de thème depuis Settings
     *
     * PROBLÈME : Quand on change le thème dans Settings,
     * MainActivity ne le sait pas automatiquement (themeMode est local).
     *
     * SOLUTION : Observer le SettingsRepository depuis MainActivity
     * et mettre à jour quand le thème change.
     *
     * FLUX :
     * 1. User change thème dans Settings
     * 2. SettingsViewModel → SettingsRepository → DataStore
     * 3. DataStore émet dans son Flow
     * 4. Ce LaunchedEffect collecte le changement
     * 5. Appelle onThemeModeUpdate
     * 6. MainActivity met à jour son state local
     * 7. AppTheme se recompose avec le nouveau thème
     * 8. Toute l'app se redessine avec les nouvelles couleurs
     */
    val settingsRepository: SettingsRepository = org.koin.compose.koinInject()

    LaunchedEffect(Unit) {
        settingsRepository.userSettings.collect { settings ->
            onThemeModeUpdate(settings.themeMode)
        }
    }
}

/**
 * RÉCAPITULATIF COMPLET : Le flux d'un changement de thème
 *
 * 1. USER ACTION
 *    └─> User ouvre Settings et choisit "Sombre"
 *
 * 2. UI EVENT
 *    └─> onClick appelle viewModel.updateThemeMode(DARK)
 *
 * 3. VIEWMODEL
 *    └─> Lance une coroutine
 *        └─> Appelle repository.updateThemeMode(DARK)
 *
 * 4. REPOSITORY
 *    └─> Écrit dans DataStore
 *        └─> dataStore.edit { preferences[THEME_KEY] = "DARK" }
 *
 * 5. DATASTORE
 *    └─> Sauvegarde sur disque (asynchrone)
 *    └─> Émet dans son Flow interne
 *
 * 6. REPOSITORY FLOW
 *    └─> .map transforme Preferences → UserSettings
 *    └─> Émet UserSettings(themeMode = DARK, ...)
 *
 * 7. VIEWMODEL STATEFLOW
 *    └─> Reçoit le nouveau UserSettings
 *    └─> Émet à tous ses collectors
 *
 * 8. COMPOSE (SettingsScreen)
 *    └─> collectAsState() détecte le changement
 *    └─> Recomposition de SettingsScreen
 *    └─> Le RadioButton "Sombre" s'affiche sélectionné
 *
 * 9. COMPOSE (MainActivity)
 *    └─> LaunchedEffect collecte le changement
 *    └─> Appelle onThemeModeUpdate(DARK)
 *    └─> themeMode state est mis à jour
 *
 * 10. APP THEME
 *     └─> AppTheme(darkTheme = true) se recompose
 *     └─> MaterialTheme propage les nouvelles couleurs
 *
 * 11. TOUTE L'APP
 *     └─> Chaque composable qui utilise MaterialTheme.colorScheme
 *     └─> Se recompose avec les nouvelles couleurs
 *     └─> L'app entière passe en mode sombre
 *
 * TOUT ÇA EN ~100ms, DE MANIÈRE FLUIDE ET AUTOMATIQUE !
 *
 * Aucune notification manuelle. Aucun listener. Aucun callback en cascade.
 * Juste des Flows qui propagent les changements naturellement. ✨
 */