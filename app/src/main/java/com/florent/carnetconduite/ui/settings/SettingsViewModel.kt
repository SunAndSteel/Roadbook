package com.florent.carnetconduite.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.carnetconduite.domain.models.UserSettings
import com.florent.carnetconduite.repository.SettingsRepository
import com.florent.carnetconduite.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran de settings.
 *
 * RÔLE PRINCIPAL : Convertir le Cold Flow du repository en Hot StateFlow pour l'UI.
 *
 * QUESTION : Pourquoi cette conversion est-elle nécessaire ?
 *
 * RÉPONSE : Imaginez le scénario sans conversion :
 *
 * Scénario A (Sans StateFlow - juste le Flow du repository) :
 * 1. SettingsScreen collecte le Flow
 *    → Nouvelle lecture de DataStore
 * 2. HomeScreen collecte aussi le Flow (pour le thème)
 *    → DEUXIÈME lecture de DataStore (c'est un cold flow!)
 * 3. User rotate l'écran
 *    → SettingsScreen se recompose
 *    → TROISIÈME lecture de DataStore
 *
 * Problèmes :
 * - Lectures multiples inutiles (performance)
 * - Pas de cache en mémoire
 * - Si les settings changent, chaque collector doit être notifié séparément
 *
 * Scénario B (Avec StateFlow - ce qu'on fait) :
 * 1. ViewModel démarre → collecte le Flow UNE FOIS
 *    → Une lecture initiale de DataStore
 *    → Convertit en StateFlow (hot)
 * 2. SettingsScreen collecte le StateFlow
 *    → Reçoit instantanément la valeur actuelle (pas de lecture disque!)
 * 3. HomeScreen collecte aussi le StateFlow
 *    → Reçoit la MÊME valeur (pas de nouvelle lecture!)
 * 4. Settings changent dans DataStore
 *    → Le Flow du repository émet
 *    → Le StateFlow est mis à jour
 *    → TOUS les collectors sont notifiés automatiquement
 *
 * Avantages :
 * ✅ Une seule "connexion" au repository
 * ✅ Cache en mémoire (StateFlow garde la valeur)
 * ✅ Tous les collectors voient le même état
 * ✅ Survit aux recompositions (dans le ViewModel)
 *
 * C'est l'essence du pattern ViewModel : CENTRALISER et CACHER l'état !
 */
class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    /**
     * StateFlow des settings, exposé à l'UI.
     *
     * MAGIE : stateIn()
     * Cette fonction convertit un Cold Flow en Hot StateFlow.
     *
     * Décortiquons les paramètres :
     */
    val userSettings: StateFlow<UserSettings> = repository.userSettings
        .stateIn(
            /**
             * PARAMÈTRE 1 : scope
             *
             * viewModelScope = CoroutineScope lié au cycle de vie du ViewModel
             *
             * Quand le ViewModel est détruit (app fermée), ce scope est annulé,
             * donc la collection du Flow s'arrête automatiquement.
             * Pas de fuite mémoire !
             *
             * QUESTION : Que se passerait-il si on utilisait GlobalScope ?
             * Réponse : Le Flow continuerait de collecter même après la destruction
             * du ViewModel → fuite mémoire classique !
             */
            scope = viewModelScope,

            /**
             * PARAMÈTRE 2 : started
             *
             * SharingStarted.WhileSubscribed(5000) = stratégie de partage
             *
             * Qu'est-ce que ça signifie ?
             * - Le StateFlow reste actif tant qu'il y a au moins un subscriber
             * - Quand le dernier subscriber se désabonne, on attend 5000ms
             * - Si personne ne se réabonne pendant ces 5s, on arrête la collection
             * - Si quelqu'un se réabonne, on recommence instantanément
             *
             * POURQUOI 5000ms ?
             * C'est un délai pour les changements de configuration (rotation écran).
             * Si l'utilisateur rotate son écran :
             * 1. L'ancienne composition se désabonne
             * 2. On attend 5s (pas besoin de stopper immédiatement)
             * 3. La nouvelle composition se réabonne (< 5s)
             * 4. Pas besoin de recharger depuis DataStore !
             *
             * ALTERNATIVES :
             * - SharingStarted.Eagerly : démarre immédiatement, ne s'arrête jamais
             * - SharingStarted.Lazily : démarre au premier subscriber, ne s'arrête jamais
             * - WhileSubscribed(0) : s'arrête dès qu'il n'y a plus de subscriber
             *
             * QUESTION : Quelle stratégie choisir pour les settings ?
             * Réponse : WhileSubscribed(5000) est parfait car :
             * - Économise les ressources quand l'app est en background
             * - Survit aux rotations d'écran
             * - Se réveille rapidement si besoin
             */
            started = SharingStarted.WhileSubscribed(5000),

            /**
             * PARAMÈTRE 3 : initialValue
             *
             * Valeur initiale du StateFlow avant que le Flow du repository émette.
             *
             * POURQUOI UserSettings.DEFAULT ?
             * - StateFlow DOIT toujours avoir une valeur
             * - DataStore prend quelques ms pour lire depuis le disque
             * - Pendant ce temps, l'UI a besoin d'une valeur à afficher
             * - On utilise les defaults pour éviter un écran de chargement
             *
             * ALTERNATIVE : UserSettings.DEFAULT ou un état Loading ?
             * Pour les settings, DEFAULT est mieux car :
             * - Pas de flicker d'écran de chargement
             * - Les valeurs par défaut sont raisonnables
             * - La vraie valeur arrive en < 100ms de toute façon
             */
            initialValue = UserSettings.DEFAULT
        )

    /**
     * Met à jour le mode du thème.
     *
     * PATTERN : Launch dans viewModelScope
     *
     * Pourquoi launch ?
     * - repository.updateThemeMode est suspend (asynchrone)
     * - On ne veut pas bloquer le thread UI
     * - viewModelScope gère l'annulation automatiquement
     *
     * FLOW AUTOMATIQUE après cet appel :
     * 1. Cette fonction écrit dans DataStore
     * 2. Le Flow repository.userSettings émet la nouvelle valeur
     * 3. Notre StateFlow userSettings reçoit la mise à jour
     * 4. Compose se recompose automatiquement
     *
     * Pas besoin de faire quoi que ce soit d'autre !
     * C'est la beauté de l'architecture réactive.
     */
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.updateThemeMode(themeMode)
        }
    }

    /**
     * Met à jour le guide par défaut.
     */
    fun updateDefaultGuide(guide: String) {
        viewModelScope.launch {
            repository.updateDefaultGuide(guide)
        }
    }

    /**
     * Met à jour l'option de confirmation de suppression.
     */
    fun updateShowDeleteConfirmations(show: Boolean) {
        viewModelScope.launch {
            repository.updateShowDeleteConfirmations(show)
        }
    }

    /**
     * Met à jour le format de date.
     */
    fun updateDateFormat(format: String) {
        viewModelScope.launch {
            repository.updateDateFormat(format)
        }
    }

    /**
     * Réinitialise tous les settings.
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaults()
        }
    }
}

/**
 * DIAGRAMME : Flow de données complet
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │                         DATASTORE                            │
 * │                    (Disque - Persistance)                   │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │ { "theme_mode": "DARK", "default_guide": "1", ... } │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────┘
 *                              │
 *                              │ dataStore.data (Flow)
 *                              ↓
 * ┌─────────────────────────────────────────────────────────────┐
 * │                      REPOSITORY                              │
 * │                                                              │
 * │  val userSettings: Flow<UserSettings> =                     │
 * │      dataStore.data.map { prefs -> UserSettings(...) }      │
 * │                                                              │
 * │  COLD FLOW : Nouvelle lecture à chaque collection          │
 * └─────────────────────────────────────────────────────────────┘
 *                              │
 *                              │ .stateIn()
 *                              ↓
 * ┌─────────────────────────────────────────────────────────────┐
 * │                       VIEWMODEL                              │
 * │                                                              │
 * │  val userSettings: StateFlow<UserSettings> =                │
 * │      repository.userSettings.stateIn(...)                   │
 * │                                                              │
 * │  HOT FLOW : Une seule "connexion" au repository             │
 * │  Cache : Garde la valeur actuelle en mémoire                │
 * │  Partage : Tous les collectors reçoivent la même valeur     │
 * └─────────────────────────────────────────────────────────────┘
 *                    │                        │
 *                    │                        │
 *        .collectAsState()          .collectAsState()
 *                    │                        │
 *                    ↓                        ↓
 *          ┌─────────────────┐      ┌──────────────────┐
 *          │ SETTINGS SCREEN │      │   HOME SCREEN    │
 *          │                 │      │  (pour le thème) │
 *          │  val settings   │      │   val settings   │
 *          │  by collectAs   │      │   by collectAs   │
 *          │  State()        │      │   State()        │
 *          └─────────────────┘      └──────────────────┘
 *
 * FLUX DE CHANGEMENT (de bas en haut) :
 *
 * 1. User clique → updateThemeMode(DARK)
 * 2. ViewModel → repository.updateThemeMode(DARK)
 * 3. Repository → dataStore.edit { theme = DARK }
 * 4. DataStore écrit sur disque + émet dans son Flow
 * 5. Repository.userSettings reçoit et transforme
 * 6. ViewModel.userSettings (StateFlow) se met à jour
 * 7. Tous les collectAsState() détectent le changement
 * 8. Compose recompose les écrans concernés
 * 9. UI reflète le nouveau thème
 *
 * TOUT EST AUTOMATIQUE ! Aucune notification manuelle.
 */