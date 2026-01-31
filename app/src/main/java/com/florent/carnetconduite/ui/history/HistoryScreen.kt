package com.florent.carnetconduite.ui.history

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is UiEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                is UiEvent.ShowConfirmDialog -> Unit
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatsHeader(
                totalDistance = tripStats.totalKm,
                totalDuration = tripStats.totalDurationMs,
                tripCount = tripStats.totalTrips,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (tripGroups.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun trajet dans l'historique",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(tripGroups) { group ->
                TripGroupCard(
                    tripGroup = group,
                    onEdit = { selectedGroupForEdit = group },
                    onDelete = { viewModel.deleteTripGroup(group) }
                )
            }
        }
    }

    selectedGroupForEdit?.let { group ->
        EditTripGroupDialog(
            group = group,
            onDismiss = { selectedGroupForEdit = null },
            onEditStartTime = { trip, newTime -> viewModel.editStartTime(trip.id, newTime) },
            onEditEndTime = { trip, newTime -> viewModel.editEndTime(trip.id, newTime) },
            onEditStartKm = { trip, newKm -> viewModel.editStartKm(trip.id, newKm) },
            onEditEndKm = { trip, newKm -> viewModel.editEndKm(trip.id, newKm) },
            onEditDate = { trip, newDate -> viewModel.editDate(trip.id, newDate) },
            onEditConditions = { trip, newConditions -> viewModel.editConditions(trip.id, newConditions) }
        )
    }
}
