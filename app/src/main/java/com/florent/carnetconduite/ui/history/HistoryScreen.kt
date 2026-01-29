package com.florent.carnetconduite.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.ui.UiEvent
import com.florent.carnetconduite.ui.home.components.StatsHeader
import com.florent.carnetconduite.ui.history.components.TripGroupCard
import com.florent.carnetconduite.ui.history.dialogs.EditTripGroupDialog
import org.koin.androidx.compose.koinViewModel

/**
 * Écran History - affiche l'historique des trajets
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val tripGroups by viewModel.tripGroups.collectAsState(initial = emptyList())
    val tripStats by viewModel.tripStats.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedGroupForEdit by remember { mutableStateOf<TripGroup?>(null) }

    // Gestion des événements UI
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
                is UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
                is UiEvent.ShowConfirmDialog -> {
                    // Géré par les cards
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header avec statistiques
            StatsHeader(
                totalDistance = tripStats.totalKm,
                totalDuration = (tripStats.totalHours * 3600000).toLong(), // hours → ms
                tripCount = tripStats.totalTrips,
                goalDistance = 1500,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Liste des trajets
            if (tripGroups.isEmpty()) {
                // État vide
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "Aucun trajet dans l'historique",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tripGroups) { group ->
                        TripGroupCard(
                            tripGroup = group,
                            onEdit = {
                                selectedGroupForEdit = group
                            },
                            onDelete = {
                                viewModel.deleteTripGroup(group)
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogue d'édition
    selectedGroupForEdit?.let { group ->
        EditTripGroupDialog(
            group = group,
            onDismiss = {
                selectedGroupForEdit = null
            },
            onEditStartTime = { trip, newTime ->
                viewModel.editStartTime(trip.id, newTime)
            },
            onEditEndTime = { trip, newTime ->
                viewModel.editEndTime(trip.id, newTime)
            },
            onEditStartKm = { trip, newKm ->
                viewModel.editStartKm(trip.id, newKm)
            },
            onEditEndKm = { trip, newKm ->
                viewModel.editEndKm(trip.id, newKm)
            },
            onEditDate = { trip, newDate ->
                viewModel.editDate(trip.id, newDate)
            },
            onEditConditions = { trip, newConditions ->
                viewModel.editConditions(trip.id, newConditions)
            }
        )
    }
}
