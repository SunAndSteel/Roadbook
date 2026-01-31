# AGENTS.md — Architecture & Refactor Rules (Bold Mode)

## Project Context
Android app in Kotlin using:
- Jetpack Compose (Material 3)
- MVVM
- Koin (DI)
- Room + Flow

The app is functional and actively developed.

**Goal of refactors:** improve structure, readability, maintainability, and performance — even if it requires temporary breakage — as long as everything is repaired and verified.

---

## Prime Directive (NON-NEGOTIABLE)
**You may break code. You must fix it. You must verify it.**

Refactors are allowed to:
- rename files/classes/functions
- delete dead code
- move code across packages
- change function signatures (including ViewModel/public APIs)
- reshape UI state models
- split/merge modules/components
- simplify architecture when it removes duplication

But every refactor must end with:
- compilation success
- tests passing (when present)
- critical user flows validated

**Temporary breakage is acceptable; unfinished breakage is not.**

---

## Definition of Done (for any change)
A change is done only when all of these are true:
1) `./gradlew test` passes (or you clearly state which tests don’t exist / aren’t runnable and why)
2) `./gradlew assembleDebug` (or `build`) succeeds
3) App runs and the main flows are manually sanity-checked:
    - start trip → outward active → arrived → return ready/active (if applicable) → completed
    - history loads and UI does not crash
4) No obvious regressions in UI/UX (same or better)

If you introduce a breaking change:
- update all call sites
- migrate data/state if needed
- remove/replace adapters once migration is complete (don’t leave “temporary hacks” behind unless justified)

---

## Core Architectural Principle
This is a **state-driven UI**.

User experience is defined by:
- ViewModel public APIs
- screen state transitions
- existing user flows

**Unlike conservative rulesets:** you are allowed to change those APIs and transitions **if** you update all references and keep user behavior coherent.

---

## Layer Responsibilities

### UI Layer (Compose)
- Prefer stateless composables.
- UI collects `StateFlow` with `collectAsState()` / `collectAsStateWithLifecycle()`.
- No repository access from UI.
- No business logic in Composables.
- UI should express: **render(state) + dispatch(intent)**.

Allowed refactors:
- extract reusable UI components
- merge multiple screens into one state-driven screen
- delete redundant composables
- rename components to match their job

### ViewModel Layer
- Owns UI state and transitions.
- Transforms Flow using `map`, `combine`, `stateIn`.
- Exposes:
    - `StateFlow` for UI state
    - intent-style functions (start, finish, cancel, edit…)

Allowed refactors:
- change public API names/signatures to improve clarity
- consolidate multiple ViewModels if it reduces duplication
- rewrite state machine if it becomes simpler and more explicit

Rule:
- After refactor, UI should be simpler, not more clever.

### Domain Layer
- Use cases + pure logic.
- No Android deps.
- No UI state.

Allowed refactors:
- merge/split use cases
- rename for clarity
- replace “boolean soup” with sealed types / value objects

### Data Layer
- Room = single source of truth.
- Repositories expose `Flow<T>` (or suspend functions for writes).
- Repository methods must represent explicit intent:
    - `finishTrip` ≠ `finishAndPrepareReturn`
    - no hidden side effects

Allowed refactors:
- rename repository functions to reflect intent
- change return types to more explicit models
- remove unused DAOs/entities/mappers

---

## State & Navigation Rules
- State transitions must be explicit and predictable.
- A "simple trip" MUST NOT create/prepare a return trip.
- A "round trip" MUST be triggered only by explicit user intent.
- No automatic transitions based solely on data presence.

Refactor freedom:
- You may redesign the state machine, but you must keep the same real-world behavior (what the user experiences), unless the change is clearly an improvement and you update UI accordingly.

---

## Flow Usage Rules
- Never treat a `Flow` as a value.
- Use:
    - `stateIn(viewModelScope, …)` for UI state
    - `first()` only for one-shot computations
- Never call `collect()` inside Composables.

Allowed refactors:
- replace nested Flows with clearer derived state
- reduce recompositions / over-collection
- add `distinctUntilChanged()` where meaningful

---

## Refactor Policy (BOLD, NOT TIMID)
**Prefer the best design, not the smallest change.**

You are encouraged to:
- remove duplication aggressively
- rename confusing things
- collapse over-engineered layers
- delete code that exists “just in case”
- change signatures to make illegal states unrepresentable

Constraints:
- No new dependencies unless explicitly approved.
- No architectural rewrites purely for aesthetics — every major change must pay rent (less code, fewer bugs, clearer flows, better UX).

If you choose between:
- adapters/wrappers vs. breaking change + cleanup  
  Prefer **breaking change + cleanup**, provided you fully migrate and verify.

---

## Cleanup Rules
Dead code should be removed if:
- not referenced
- behavior fully replaced
- no pending TODO that blocks deletion

When deleting:
- remove associated tests or update them
- remove Koin bindings
- remove unused resources (strings, icons, themes) if clearly dead

Do not keep “legacy adapters” unless:
- required for staged migration
- or prevents a large risky change  
  If kept, document why and when it can be removed.

---

## UI/UX Rules
- Material Design 3 components only.
- One user journey = one continuous experience.
- Screens are content variations, not separate pages.
- Reduce redundancy and cognitive load.
- Important moments (arrival, completion) must be visually emphasized.

Allowed refactors:
- merge multiple “state screens” into one polished screen with conditional sections
- reorganize cards/components for clarity
- remove redundant actions and texts

---

## Error Handling (Production-grade)
User-visible errors must be rare, non-technical, actionable.

Prefer:
1) inline validation
2) snackbar with retry
3) dialog for blocking choices

Avoid:
- raw exception text in UI
- error spam

Separation:
- Data/Domain produce typed failures or `Result`
- ViewModel maps failures → user-friendly messages
- Full details logged via AppLogger (tag + throwable + context)

---

## How to Work (Agent Behavior)
When modifying code:
1) Identify the core flow and state machine.
2) Decide the cleanest target design.
3) Make the change decisively (rename/move/delete as needed).
4) Repair all breakage immediately (no half-migrations).
5) Run checks:
    - tests
    - build
    - sanity flows
6) Summarize what changed + why, including any signature changes.

Do NOT:
- stop after “it compiles” if flows are broken
- leave TODOs where the refactor should be finished
- keep redundant code “just in case”
