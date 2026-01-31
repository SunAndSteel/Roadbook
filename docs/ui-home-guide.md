# Guide de l’UI Home (Jetpack Compose + Flow + MVVM)

Ce document explique **en détail** comment fonctionne l’interface *Home* de l’application. Il s’adresse à un·e développeur·euse qui a une solide base en programmation mais **aucune familiarité** avec Jetpack Compose, Kotlin Flow ou l’implémentation MVVM spécifique à ce projet.

---

## 1) Vue d’ensemble rapide

L’écran *Home* est un **parcours utilisateur** géré par un **état de conduite** (`DrivingState`). L’UI ne “devine” rien : **chaque changement d’état est explicite** et piloté par le `HomeViewModel`. Concrètement :

- Le **ViewModel** observe les données (`Trip`) via des `Flow` venant du repository.
- Il calcule l’**état de conduite** (`IDLE`, `OUTWARD_ACTIVE`, `ARRIVED`, `RETURN_READY`, `RETURN_ACTIVE`, `COMPLETED`).
- L’UI **rend un écran différent** selon cet état.

Les fichiers principaux sont :

- `HomeScreen.kt` : composition globale de l’écran et orchestration UI.
- `HomeViewModel.kt` : logique d’état, transitions et actions utilisateur.
- `HomeUiState.kt` : état UI local (champs de formulaires + flags de dialogues).
- Dossiers `sections/` et `components/` : composables réutilisables.

---

## 2) Concepts de base (si tu ne connais pas Compose/Flow/MVVM)

### Jetpack Compose en deux minutes

- Compose est un **UI toolkit déclaratif**. On décrit “à quoi ressemble l’UI” en fonction d’un état.
- Un `@Composable` est une fonction qui **décrit** une partie de l’interface.
- Quand un état change, Compose **recompose** automatiquement les parties concernées.

👉 Ici, `HomeScreen` est un `@Composable` qui assemble l’interface en fonction de `DrivingState`.

### Kotlin Flow en deux minutes

- `Flow` est un flux de données **asynchrones**.
- L’UI s’abonne à un `Flow` avec `collectAsState()` pour obtenir un `State<T>` qui déclenche la recomposition.

👉 Dans `HomeScreen`, on fait par exemple :

```kotlin
val drivingState by viewModel.drivingState.collectAsState()
```

### MVVM en deux minutes

- **Model** : les données (ici `Trip`, la base de données, le repository).
- **ViewModel** : transforme les données en état UI et expose des actions.
- **View** : l’UI (Compose) se contente d’afficher l’état et de relayer les actions.

👉 Les fonctions `startOutward`, `finishOutward`, `decideTripType` etc. sont **des intentions explicites** côté ViewModel.

---

## 3) Cheminement de l’UI Home

### 3.1 État principal : `DrivingState`

Le `HomeViewModel` expose un `StateFlow<DrivingState>` calculé à partir des trajets (`Trip`). Le calcul est fait dans `drivingState` via `ComputeDrivingStateUseCase` et un petit *latch* pour l’état `COMPLETED`.

L’UI affiche **une section différente** selon cet état :

- `IDLE` : formulaire de départ.
- `OUTWARD_ACTIVE` : formulaire d’arrivée pour l’aller.
- `ARRIVED` : décision retour ou trajet simple.
- `RETURN_READY` : vérification du départ retour.
- `RETURN_ACTIVE` : fin du retour.
- `COMPLETED` : écran de confirmation.

### 3.2 État UI local : `HomeUiState`

L’UI a besoin d’**états locaux** (texte saisi, cases ouvertes, dialogues). On les centralise dans `HomeUiState` :

- `IdleFormState` : champs de départ (`startKm`, `startPlace`, etc.).
- `OutwardActiveFormState` : champs d’arrivée aller (`endKm`, `endPlace`).
- `ArrivalInputsState` : champs d’arrivée (sticky) + flags de dialogs.
- `ReturnReadyFormState`, `ReturnActiveFormState` : champs de retour.

Cet état est créé par `rememberHomeUiState()` dans `HomeScreen` et passé aux sections.

---

## 4) Structure de l’écran Home

### 4.1 Composition principale (`HomeScreen`)

`HomeScreen` fait trois choses :

1. **Collecter l’état** :
   - `drivingState`, `trips`, `tripGroups` via `collectAsState()`.

2. **Choisir le contenu** :
   - Les trajets actifs sont regroupés via `HomeTripSnapshot` pour éviter les duplications.
   - Le contenu central est rendu par `HomeScrollableContent`, un `Column` scrollable.
   - Chaque état appelle un composable de `sections/` via `HomeFormSection`.

3. **Afficher la zone sticky** :
   - `StickyBottomArea` affiche un CTA principal adapté à l’état.
   - En mode `ARRIVED`, elle affiche aussi un petit formulaire d’arrivée en bas.

`HomeScreen` délègue aussi :
- `HomeDialogs` pour centraliser l’affichage des dialogues d’édition.

### 4.2 En-tête dynamique (header)

Le header est piloté par :

- `headerForDrivingState()` : texte + icône selon l’état.
- `colorsForDrivingState()` : palette adaptée à l’étape.

Le résultat est rendu par `TripHeaderCompact()`.

### 4.3 Résumé du trajet

Quand un trajet est actif ou terminé, `TripSummaryHeader` affiche un résumé minimal (ex. horaires). Cela évite de dupliquer le statut ailleurs.

---

## 5) Actions utilisateur et événements UI

### 5.1 Actions (intentions)

Les boutons de l’UI appellent **directement** des fonctions du ViewModel :

- `startOutward(...)` : démarrer l’aller.
- `finishOutward(...)` : terminer l’aller.
- `prepareReturnTrip(...)` : préparer un retour.
- `confirmSimpleTrip(...)` : valider un trajet simple.
- `startReturn(...)` : démarrer le retour.
- `finishReturn(...)` : terminer le retour.
- `cancelReturn(...)` : annuler un retour.

**Important** : aucune logique métier n’est dans les composables.

### 5.2 Événements UI (`UiEvent`)

Le ViewModel envoie des événements ponctuels (snackbar, confirmation) via `uiEvent` :

- `UiEvent.ShowToast` / `ShowError` → snackbar.
- `UiEvent.ShowConfirmDialog` → confirmation explicite.

`HomeScreen` écoute ce flux via `LaunchedEffect` et affiche les messages.

---

## 6) Dialogues et édition inline

Les dialogues d’édition sont ouverts via des flags d’état (ex. `showEditEndTime`). Ils sont affichés **au bas de `HomeScreen`** via :

- `OutwardActiveFormDialogs(...)`
- `ReturnActiveFormDialogs(...)`
- `ArrivedDecisionDialogs(...)`

Chaque dialogue applique une modification via `HomeViewModel.edit...()`.

---

## 7) Où modifier quoi ?

### UI pure (composables)

- **Sections** : `ui/home/sections/*`
  - Contiennent les formulaires principaux par état.
- **Components** : `ui/home/components/*`
  - Petites briques réutilisables (header, CTA, sticky).

### Logique d’état

- `HomeViewModel.kt`
  - API d’intentions et gestion des erreurs.
- `HomeUiState.kt`
  - État local des formulaires et dialogues.

### Mapping visuel

- `ui/home/mapper/HomeScreenMappings.kt`
  - Associe état → icônes / textes / couleurs.

---

## 8) Règles à respecter dans ce projet

1. **UI sans logique métier** : ne jamais appeler un repository depuis l’UI.
2. **Transitions explicites** : pas de “retour automatique” si des données existent.
3. **Flow → StateFlow** : toujours utiliser `stateIn` côté ViewModel et `collectAsState()` côté UI.
4. **Pas de collecte manuelle en UI** : pas de `collect()` dans un composable.

---

## 9) Exemple mental d’un trajet

1. `IDLE` : l’utilisateur saisit départ puis clique “Démarrer”.
2. `OUTWARD_ACTIVE` : il saisit l’arrivée puis “Terminer”.
3. `ARRIVED` : il choisit retour **ou** trajet simple.
4. Si retour :
   - `RETURN_READY` : confirme départ retour.
   - `RETURN_ACTIVE` : termine le retour.
5. `COMPLETED` : confirmation finale.

Chaque étape correspond à un composable spécifique et un CTA dans la sticky area.

---

## 10) Conseils pour contribuer sans casser l’UI

- Ne change pas les signatures exposées si elles sont utilisées par un écran.
- Si tu dois “adapter” un ancien flux, crée un wrapper plutôt qu’un breaking change.
- Vérifie toujours que `DrivingState` conduit à **une** section et **un** CTA cohérents.
- Les erreurs visibles pour l’utilisateur doivent être courtes et actionnables.

---

## Références rapides

- `HomeScreen.kt` (composition générale)
- `HomeViewModel.kt` (logique + intents)
- `HomeUiState.kt` (état UI local)
- `ui/home/sections/*` (écrans par état)
- `ui/home/components/*` (briques UI)
- `HomeScreenMappings.kt` (mapping visuel)
