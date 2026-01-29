package com.florent.carnetconduite.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.domain.models.UserSettings
import com.florent.carnetconduite.ui.theme.ThemeMode

/**
 * Écran de paramètres de l'application.
 *
 * ARCHITECTURE COMPOSE :
 *
 * Ce composable est STATELESS. Il :
 * - Reçoit les données en paramètre (settings)
 * - Notifie les changements via des callbacks (onXxxChange)
 * - Ne possède PAS l'état, il le REFLÈTE
 *
 * POURQUOI cette approche ?
 * 1. TESTABILITÉ : Facile de tester (juste passer des valeurs et vérifier les callbacks)
 * 2. RÉUTILISABILITÉ : Pourrait être utilisé avec un autre ViewModel
 * 3. PRÉVISIBILITÉ : L'état vit dans le ViewModel, pas dispersé partout
 * 4. DEBUGGING : Facile de voir d'où vient l'état (un seul endroit)
 *
 * PATTERN : Container/Presenter
 * - Container (ci-dessous) : Observe le ViewModel et passe les données
 * - Presenter (cette fonction) : Affiche les données, pas d'état local
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDefaultGuideChange: (String) -> Unit,
    onShowDeleteConfirmationsChange: (Boolean) -> Unit,
    onResetToDefaults: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    /**
     * État LOCAL pour les dialogues.
     *
     * QUESTION : Pourquoi cet état est-il local et pas dans le ViewModel ?
     *
     * Réponse : Les dialogues sont purement UI, pas de logique métier.
     * L'état "le dialogue est ouvert" n'a pas besoin de persister :
     * - Si l'app est tuée, on s'en fiche que le dialogue était ouvert
     * - Si on rotate l'écran, c'est OK de fermer le dialogue
     * - C'est un détail d'implémentation de l'UI
     *
     * RÈGLE : État dans ViewModel = important pour le métier
     *         État local = détail d'affichage temporaire
     */
    var showThemeDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ============ SECTION APPARENCE ============
            SettingsSectionHeader("Apparence")

            /**
             * Setting du thème.
             *
             * PATTERN : Clickable avec dialogue
             * - Cliquer ouvre un dialogue de choix
             * - Le dialogue est un état UI local (showThemeDialog)
             * - Le changement réel passe par onThemeModeChange
             *
             * FLUX :
             * 1. User clique → showThemeDialog = true
             * 2. Dialogue s'affiche
             * 3. User choisit → onThemeModeChange(newMode)
             * 4. ViewModel → Repository → DataStore
             * 5. Flow remonte → StateFlow → collectAsState()
             * 6. Recomposition avec nouveau settings.themeMode
             * 7. L'UI affiche la nouvelle valeur
             */
            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Thème",
                subtitle = when (settings.themeMode) {
                    ThemeMode.LIGHT -> "Clair"
                    ThemeMode.DARK -> "Sombre"
                    ThemeMode.DYNAMIC -> "Dynamique"
                },
                onClick = { showThemeDialog = true }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // ============ SECTION TRAJETS ============
            SettingsSectionHeader("Trajets")

            SettingsItem(
                icon = Icons.Default.Person,
                title = "Guide par défaut",
                subtitle = "Guide ${ settings.defaultGuide}",
                onClick = { showGuideDialog = true }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            /**
             * Switch pour les confirmations de suppression.
             *
             * PATTERN : Switch avec état
             * - Le Switch est directement lié à settings.showDeleteConfirmations
             * - Changer le Switch appelle onShowDeleteConfirmationsChange
             * - Pas besoin de dialogue, c'est binaire (on/off)
             *
             * QUESTION : Pourquoi un Switch ici mais un dialogue pour le thème ?
             * Réponse :
             * - Boolean (2 valeurs) → Switch/Checkbox (immédiat)
             * - Enum (3+ valeurs) → Dialogue de choix (évite d'encombrer)
             */
            SettingsSwitchItem(
                icon = Icons.Default.Delete,
                title = "Confirmer les suppressions",
                subtitle = "Demander confirmation avant de supprimer un trajet",
                checked = settings.showDeleteConfirmations,
                onCheckedChange = onShowDeleteConfirmationsChange
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // ============ SECTION AVANCÉ ============
            SettingsSectionHeader("Avancé")

            SettingsItem(
                icon = Icons.Default.RestartAlt,
                title = "Réinitialiser les paramètres",
                subtitle = "Restaurer les valeurs par défaut",
                onClick = { showResetDialog = true },
                // Couleur rouge pour indiquer une action "destructive"
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    // ============ DIALOGUES ============

    /**
     * Dialogue de sélection du thème.
     *
     * PATTERN : Dialogue avec RadioButton
     * Les RadioButtons montrent toutes les options avec celle sélectionnée.
     */
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = settings.themeMode,
            onThemeSelected = { newMode ->
                onThemeModeChange(newMode)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    /**
     * Dialogue de sélection du guide par défaut.
     */
    if (showGuideDialog) {
        GuideSelectionDialog(
            currentGuide = settings.defaultGuide,
            onGuideSelected = { newGuide ->
                onDefaultGuideChange(newGuide)
                showGuideDialog = false
            },
            onDismiss = { showGuideDialog = false }
        )
    }

    /**
     * Dialogue de confirmation de réinitialisation.
     *
     * PATTERN : Dialogue de confirmation
     * Pour les actions destructives, toujours demander confirmation.
     */
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("Réinitialiser les paramètres ?") },
            text = { Text("Tous vos paramètres seront restaurés à leurs valeurs par défaut.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Réinitialiser")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

/**
 * Composable pour un item de setting cliquable.
 *
 * PATTERN : Composable réutilisable
 * Au lieu de dupliquer le code pour chaque setting, on crée un composable
 * générique qu'on configure avec des paramètres.
 *
 * AVANTAGES :
 * - DRY (Don't Repeat Yourself)
 * - Cohérence visuelle garantie
 * - Facile de changer le design de tous les items
 */
@Composable
internal fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = tint
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Composable pour un item de setting avec Switch.
 */
@Composable
internal fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Header de section dans les settings.
 */
@Composable
internal fun SettingsSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * Dialogue de sélection du thème.
 *
 * PATTERN : Dialogue avec options radio
 * Chaque option est cliquable entièrement (pas juste le bouton radio).
 */
@Composable
internal fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choisir le thème") },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(mode) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == currentTheme,
                            onClick = { onThemeSelected(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when (mode) {
                                    ThemeMode.LIGHT -> "Clair"
                                    ThemeMode.DARK -> "Sombre"
                                    ThemeMode.DYNAMIC -> "Système"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = when (mode) {
                                    ThemeMode.LIGHT -> "Toujours en mode clair"
                                    ThemeMode.DARK -> "Toujours en mode sombre"
                                    ThemeMode.DYNAMIC -> "Suit les paramètres du système"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

/**
 * Dialogue de sélection du guide par défaut.
 */
@Composable
internal fun GuideSelectionDialog(
    currentGuide: String,
    onGuideSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Guide par défaut") },
        text = {
            Column {
                listOf("1", "2").forEach { guide ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGuideSelected(guide) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = guide == currentGuide,
                            onClick = { onGuideSelected(guide) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guide $guide",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    }
}

        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

/**
 * CONTAINER : Composable qui observe le ViewModel et passe les données au Presenter.
 *
 * PATTERN : Container/Presenter (Smart/Dumb Components)
 *
 * Ce composable :
 * - Observe le ViewModel (smart)
 * - Passe les données au SettingsScreen stateless (dumb)
 * - Gère les callbacks vers le ViewModel
 *
 * AVANTAGES :
 * - SettingsScreen est testable sans ViewModel
 * - SettingsScreen peut être preview dans Android Studio
 * - Séparation claire entre logique (container) et affichage (presenter)
 */
@Composable
fun SettingsScreenContainer(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    /**
     * OBSERVATION DU STATEFLOW
     *
     * collectAsState() fait le pont entre Flow (réactif) et Compose (déclaratif).
     *
     * À chaque émission du StateFlow :
     * 1. collectAsState() détecte le changement
     * 2. Met à jour le State Compose
     * 3. Déclenche une recomposition de SettingsScreen
     * 4. L'UI reflète les nouvelles valeurs
     *
     * AUTOMATIQUE ! Pas de listeners, callbacks, ou notifications manuelles.
     */
    val settings by viewModel.userSettings.collectAsState()

    SettingsScreen(
        settings = settings,
        onThemeModeChange = viewModel::updateThemeMode,
        onDefaultGuideChange = viewModel::updateDefaultGuide,
        onShowDeleteConfirmationsChange = viewModel::updateShowDeleteConfirmations,
        onResetToDefaults = viewModel::resetToDefaults,
        onNavigateBack = onNavigateBack
    )
}

/**
 * QUESTION FINALE DE RÉFLEXION :
 *
 * Pourquoi séparer SettingsScreen (presenter) et SettingsScreenContainer (container) ?
 *
 * Pourquoi pas tout mettre dans un seul composable ?
 *
 * Prenez 30 secondes pour y penser avant de voir la réponse...
 *
 *
 *
 *
 * RÉPONSE :
 *
 * 1. TESTABILITÉ
 *    SettingsScreen(
 *        settings = UserSettings(themeMode = ThemeMode.DARK),
 *        onThemeModeChange = { assertCalled() }
 *    )
 *    Pas besoin de mock du ViewModel, juste passer des valeurs !
 *
 * 2. PREVIEW
 *    @Preview
 *    @Composable
 *    fun PreviewSettings() {
 *        SettingsScreen(
 *            settings = UserSettings.DEFAULT,
 *            onThemeModeChange = {},
 *            ...
 *        )
 *    }
 *    Impossible de preview si ça nécessite un ViewModel !
 *
 * 3. RÉUTILISABILITÉ
 *    SettingsScreen pourrait être utilisé avec un autre ViewModel,
 *    ou même sans ViewModel (données hardcodées).
 *
 * 4. SÉPARATION DES RESPONSABILITÉS
 *    Container = logique réactive (Flow, ViewModel)
 *    Presenter = logique d'affichage pure (layouts, couleurs)
 *
 * C'est un pattern fondamental en Compose !
 */
