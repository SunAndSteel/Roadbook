# Architecture

## Overview
Roadbook is an Android application built with Kotlin and Jetpack Compose using a state-driven MVVM + Clean Architecture approach. UI screens are deterministic renderings of ViewModel state, and state transitions are explicit and user-driven.

## Layered Structure
- **UI Layer (Compose)**
  - Stateless composables render UI based on `StateFlow` collected with `collectAsState()`.
  - No repository access or business logic in UI.
  - Screens represent **steps of a single user journey** (e.g., trip flow) rather than isolated features.
- **ViewModel Layer**
  - Owns UI state and transitions.
  - Exposes `StateFlow` and intent-style functions (start, finish, cancel, edit).
  - Maps domain results into user-friendly UI messages and emits UI events.
- **Domain Layer**
  - Use cases encapsulate business rules and pure logic.
  - No Android dependencies.
- **Data Layer**
  - Repositories expose `Flow<T>` only.
  - Room is the single source of truth.
  - Methods are explicit and do not infer user intent.

## State & Navigation
- UI is **state-driven**: screens render based on `DrivingState` and `Trip` data provided by the ViewModel.
- Navigation/state transitions are explicit (e.g., finishing a trip, preparing a return).
- A simple trip never creates or prepares a return unless explicitly requested by the user.

## Data & Event Flow
1. **User action** in UI triggers an explicit ViewModel intent function.
2. **ViewModel** calls a domain use case.
3. **Use case** executes business logic and returns `Result`.
4. **ViewModel** updates `StateFlow` or emits a `UiEvent`.
5. **UI** reacts by rendering state or displaying a snackbar/dialog.

## Dependencies
- **Dependency injection** is provided by Koin.
- **Persistence** is handled by Room with Flows for reactive updates.

## UI Consistency Rules
- Material Design 3 components are used throughout.
- The Home journey is a continuous experience: the Home screen is the narrative container, and step screens render content within that container.
