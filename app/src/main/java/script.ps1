# Racine du package
$base = "com/florent/carnetconduite"

# Liste des dossiers à créer
$folders = @(
    "$base/domain",
    "$base/domain/models",
    "$base/domain/mappers",

    "$base/repository",

    "$base/ui",
    "$base/ui/components",
    "$base/ui/screens",
    "$base/ui/screens/states",
    "$base/ui/dialogs",
    "$base/ui/navigation",
    "$base/ui/theme"
)

# Liste des fichiers .kt à créer
$files = @(
    # domain
    "$base/domain/models/TripGroup.kt",

    # ui/components
    "$base/ui/components/StatsHeader.kt",
    "$base/ui/components/TripGroupCard.kt",
    "$base/ui/components/TripDetails.kt",

    # ui/screens
    "$base/ui/screens/HomeScreen.kt",
    "$base/ui/screens/HistoryScreen.kt",

    # ui/screens/states
    "$base/ui/screens/states/IdleScreen.kt",
    "$base/ui/screens/states/OutwardActiveScreen.kt",
    "$base/ui/screens/states/ArrivedScreen.kt",
    "$base/ui/screens/states/ReturnReadyScreen.kt",
    "$base/ui/screens/states/ReturnActiveScreen.kt",
    "$base/ui/screens/states/CompletedScreen.kt",

    # ui/dialogs
    "$base/ui/dialogs/TimePickerDialog.kt",
    "$base/ui/dialogs/EditKmDialog.kt",
    "$base/ui/dialogs/EditTripGroupDialog.kt",

    # ui/navigation
    "$base/ui/navigation/NavGraph.kt",

    # ui (root)
    "$base/ui/DrivingViewModel.kt",
    "$base/ui/UiEvent.kt",

    # root
    "$base/MainActivity.kt"
)

Write-Host "Création des dossiers..."
foreach ($folder in $folders) {
    if (-Not (Test-Path $folder)) {
        New-Item -ItemType Directory -Path $folder | Out-Null
        Write-Host "$folder"
    }
}

Write-Host "Création des fichiers .kt vides..."
foreach ($file in $files) {
    if (-Not (Test-Path $file)) {
        New-Item -ItemType File -Path $file | Out-Null
        Write-Host "$file"
    }
}

Write-Host "`Arborescence Kotlin initialisée avec succès."
