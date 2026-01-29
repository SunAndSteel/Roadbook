package com.florent.carnetconduite.ui.history

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.ui.UiEvent
import com.florent.carnetconduite.ui.home.components.StatsHeader
import com.florent.carnetconduite.ui.history.components.TripGroupCard
import com.florent.carnetconduite.ui.history.dialogs.EditTripGroupDialog
import kotlinx.coroutines.flow.collect
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
    val context = LocalContext.current

    var selectedGroupForEdit by remember { mutableStateOf<TripGroup?>(null) }

    // Gestion des événements UI
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is UiEvent.ShowConfirmDialog -> {
                    // Géré par les cards
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header avec statistiques
        StatsHeader(
            totalDistance = tripStats.totalKm,
            totalDuration = (tripStats.totalHours * 3600000).toLong(), // hours → ms
            tripCount = tripStats.totalTrips,
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

    // Dialogue d'édition
    selectedGroupForEdit?.let { group ->
        EditTripGroupDialog(
            group = group,
            onDismiss = {
                selectedGroupForEdit = null
            },
            onEditStartTime = { trip, newTime ->
                // TODO: Implémenter avec EditTripUseCase
            },
            onEditEndTime = { trip, newTime ->
                // TODO: Implémenter avec EditTripUseCase
            },
            onEditStartKm = { trip, newKm ->
                // TODO: Implémenter avec EditTripUseCase
            },
            onEditEndKm = { trip, newKm ->
                // TODO: Implémenter avec EditTripUseCase
            }
        )
    }
}