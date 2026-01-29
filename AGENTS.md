# AGENTS.md

This is an Android app using Kotlin, Jetpack Compose, MVVM, Koin, Room, and Flow.

The project is in a controlled refactor phase.
Existing UI screens define the required public ViewModel APIs.

Do NOT remove or rename public ViewModel properties or methods.
If a screen calls something, reintroduce it via adapters/wrappers.

Repositories expose Flow<T>, never collections or suspend getters.
ViewModels transform Flow into StateFlow using map/combine/stateIn.

UI must never access repositories directly.
Make minimal, incremental changes only. No global refactors.