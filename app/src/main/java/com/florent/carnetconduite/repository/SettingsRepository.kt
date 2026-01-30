package com.florent.carnetconduite.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.florent.carnetconduite.domain.models.UserSettings
import com.florent.carnetconduite.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository pour gérer les préférences utilisateur.
 *
 * ARCHITECTURE IMPORTANTE : Ce repository fait le pont entre deux mondes :
 *
 * 1. PERSISTANCE (DataStore) : Les données sur le disque
 *    - Survit aux redémarrages de l'app
 *    - Source de vérité ultime
 *
 * 2. RÉACTIVITÉ (Flow) : Stream de données
 *    - Émet automatiquement quand DataStore change
 *    - L'UI réagit instantanément
 *
 * QUESTION : Pourquoi ne pas simplement exposer un StateFlow ?
 *
 * Réponse : On expose un Flow<UserSettings> (cold) depuis DataStore.
 * Le ViewModel le convertira en StateFlow (hot) pour l'UI.
 *
 * Pourquoi cette séparation ?
 * - Repository = source de vérité (DataStore)
 * - ViewModel = adaptateur pour l'UI (StateFlow)
 * - Séparation des responsabilités !
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_settings"
)

class SettingsRepository(
    private val context: Context,
    private val dataStore: DataStore<Preferences> = context.settingsDataStore
) {

    /**
     * Clés pour les préférences dans DataStore.
     *
     * PATTERN : Companion object pour les constantes
     * Centralise toutes les clés au même endroit.
     */
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val DEFAULT_GUIDE_KEY = stringPreferencesKey("default_guide")
        private val SHOW_DELETE_CONFIRMATIONS_KEY = booleanPreferencesKey("show_delete_confirmations")
        private val DATE_FORMAT_KEY = stringPreferencesKey("date_format")
    }

    /**
     * Flow de settings qui émet à chaque changement dans DataStore.
     *
     * C'est un COLD FLOW car il vient directement de DataStore.
     * Chaque collector déclenche une lecture de DataStore.
     *
     * MAIS : DataStore lui-même cache les valeurs en mémoire,
     * donc pas de lecture disque à chaque fois.
     *
     * MAGIE : Le .map() transforme le Flow<Preferences> en Flow<UserSettings>
     *
     * QUESTION : Que se passe-t-il si on collecte ce flow deux fois ?
     * Réponse : Deux lectures de DataStore (c'est un cold flow!)
     * C'est pourquoi le ViewModel le convertira en StateFlow (hot).
     */
    val userSettings: Flow<UserSettings> = dataStore.data
        .map { preferences ->
            // Transformation : Preferences (clé-valeur) -> UserSettings (modèle typé)
            UserSettings(
                themeMode = preferences[THEME_MODE_KEY]?.let {
                    ThemeMode.valueOf(it)
                } ?: UserSettings.DEFAULT.themeMode,

                defaultGuide = preferences[DEFAULT_GUIDE_KEY]
                    ?: UserSettings.DEFAULT.defaultGuide,

                showDeleteConfirmations = preferences[SHOW_DELETE_CONFIRMATIONS_KEY]
                    ?: UserSettings.DEFAULT.showDeleteConfirmations,

                dateFormat = preferences[DATE_FORMAT_KEY]
                    ?: UserSettings.DEFAULT.dateFormat
            )
        }

    /**
     * Met à jour le mode du thème.
     *
     * PATTERN : Suspend function
     * Cette fonction est asynchrone car écrire dans DataStore est une opération I/O.
     *
     * FLOW AUTOMATIQUE :
     * 1. Cette fonction écrit dans DataStore
     * 2. DataStore émet automatiquement dans son Flow
     * 3. Notre Flow userSettings émet la nouvelle valeur
     * 4. Le StateFlow du ViewModel est mis à jour
     * 5. Compose se recompose
     *
     * Tout ça sans code supplémentaire ! C'est la puissance des Flows.
     */
    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    /**
     * Met à jour le guide par défaut.
     *
     * QUESTION : Pourquoi valider ici ET dans le validator ?
     * Réponse :
     * - Validator = validation lors de la création d'un trajet
     * - Ici = simple vérification que la valeur est stockable
     * Ce n'est pas de la duplication, ce sont deux responsabilités différentes.
     */
    suspend fun updateDefaultGuide(guide: String) {
        require(guide in listOf("1", "2")) {
            "Guide doit être 1 ou 2"
        }
        dataStore.edit { preferences ->
            preferences[DEFAULT_GUIDE_KEY] = guide
        }
    }

    /**
     * Met à jour l'option de confirmation de suppression.
     */
    suspend fun updateShowDeleteConfirmations(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_DELETE_CONFIRMATIONS_KEY] = show
        }
    }

    /**
     * Met à jour le format de date.
     */
    suspend fun updateDateFormat(format: String) {
        dataStore.edit { preferences ->
            preferences[DATE_FORMAT_KEY] = format
        }
    }

    /**
     * Réinitialise tous les settings aux valeurs par défaut.
     *
     * PATTERN : Transaction atomique
     * Toutes les modifications sont faites dans un seul edit { },
     * donc soit tout réussit, soit rien ne change.
     */
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
            // DataStore émettra les valeurs par défaut via le map ci-dessus
        }
    }
}

/**
 * RÉCAPITULATIF : Le parcours d'un changement de setting
 *
 * 1. USER ACTION
 *    User change le thème dans SettingsScreen
 *    ↓
 * 2. UI EVENT
 *    onClick déclenche viewModel.updateThemeMode(newMode)
 *    ↓
 * 3. VIEWMODEL
 *    Appelle repository.updateThemeMode(newMode)
 *    ↓
 * 4. REPOSITORY (cette classe)
 *    Écrit dans DataStore avec edit { }
 *    ↓
 * 5. DATASTORE
 *    Émet automatiquement dans son Flow interne
 *    ↓
 * 6. REPOSITORY FLOW
 *    Le Flow userSettings reçoit les nouvelles Preferences
 *    map { } transforme en UserSettings
 *    ↓
 * 7. VIEWMODEL STATEFLOW
 *    Le StateFlow reçoit le nouveau UserSettings
 *    Émet à tous ses collectors
 *    ↓
 * 8. COMPOSE
 *    collectAsState() détecte le changement
 *    Déclenche une recomposition
 *    ↓
 * 9. UI UPDATE
 *    L'interface reflète le nouveau thème
 *
 * TOUT ÇA est AUTOMATIQUE grâce aux Flows !
 * Pas besoin de callbacks, listeners, ou notifications manuelles.
 */
