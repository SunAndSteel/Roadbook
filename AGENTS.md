# AGENTS.md — Architecture & Refactor Rules

## Project Context
Android application written in Kotlin using:
- Jetpack Compose (Material 3)
- MVVM architecture
- Koin for dependency injection
- Room + Flow for persistence

This project is actively developed and already functional.
Refactors must preserve behavior and user experience.

---

## Core Architectural Principle (NON-NEGOTIABLE)

This is a **state-driven UI**.

User experience is defined by:
- ViewModel public APIs
- Screen state transitions
- Existing user flows

If a screen calls a property or function, it MUST exist.
Prefer adapters/wrappers over breaking changes.

---

## Layer Responsibilities

### UI Layer (Compose)
- Stateless Composables.
- UI collects `StateFlow` using `collectAsState()`.
- NO repository access from UI.
- NO business logic in Composables.
- Screens represent **steps of a user journey**, not separate features.

### ViewModel Layer
- Owns UI state and transitions.
- Transforms Flow using `map`, `combine`, `stateIn`.
- Exposes:
    - `StateFlow` for UI state
    - Explicit intent-style functions (start, finish, cancel, edit…)
- ViewModels MAY wrap legacy APIs to preserve compatibility.

### Domain Layer
- Contains use cases and pure logic.
- No Android dependencies.
- No UI state.

### Data Layer
- Repositories expose `Flow<T>` only (never raw collections).
- Room is the single source of truth.
- Repository methods must be explicit:
    - `finishTrip` ≠ `finishAndPrepareReturn`
    - Never infer user intent implicitly.

---

## State & Navigation Rules

- Navigation/state transitions must be **explicit**, never implicit.
- A "simple trip" MUST NOT create or prepare a return trip.
- A "round trip" MUST be triggered only by explicit user intent.
- No automatic transitions based solely on data presence.

---

## Flow Usage Rules

- Never treat a `Flow` as a value.
- Use:
    - `stateIn(viewModelScope, …)` for UI state
    - `first()` only for one-shot computations
- Never call `collect()` inside Composables.

---

## Refactor Constraints (VERY IMPORTANT)

- Prefer **minimal, incremental changes**.
- NO large refactors unless explicitly requested.
- NO renaming of public APIs without backward-compatible wrappers.
- NO new dependencies unless explicitly approved.
- NO architectural rewrites “for cleanliness”.

If unsure, choose the smallest safe change.

---

## Cleanup Rules

- Dead code may be removed ONLY if:
    - It is not referenced anywhere
    - Its behavior is fully migrated
- Legacy classes (e.g. old ViewModels) may temporarily exist as adapters.
- Cleanup must NEVER change behavior.

---

## UI/UX Rules

- Material Design 3 components only.
- One user journey = one continuous experience.
- Screens are content variations, not separate pages.
- Reduce cognitive load:
    - Progressive disclosure
    - Avoid redundant information
- Important moments (arrival, completion) must be visually emphasized.

---

## Error Handling

## Error Handling (PRODUCTION-GRADE)

### Core principle
User-visible errors must be **rare, non-technical, and actionable**.
Technical details must never be shown to end users.

### UI strategy
Prefer, in this order:
1) **Inline validation** (TextField supporting text, field error state) for user input issues.
2) **Snackbar** for transient failures with optional "Retry" action.
3) **Dialog** only for blocking decisions that require explicit user choice.

Avoid:
- Toasts for errors (non-actionable, inconsistent)
- Showing raw exception messages or stack traces
- Error spam (repeated messages for the same failure)

### Separation of concerns
- Data/Domain produce typed failures (or Result types).
- ViewModel maps failures to **user-friendly UI messages**.
- Full technical details are logged via AppLogger (tag + throwable + context).

### Logging rule
Always log the technical error with enough context to debug:
- operation name (e.g., finishTrip)
- tripId (if relevant)
- current state (if available)
- throwable

But expose to the user only:
- short message: “Impossible d’enregistrer. Réessaie.”
- optional action: “Réessayer”

### Defaults
When unsure, use a generic user message:
“Une erreur est survenue. Réessaie.”
and log the full details for debugging.

---

## How to Work (Codex Behavior)

When modifying code:
1. Diagnose the current architecture.
2. Identify risks of breaking behavior.
3. Propose a minimal plan.
4. Apply changes in small steps.
5. Explain architectural reasoning briefly.

Do NOT skip analysis.
Do NOT assume intent.
