package com.florent.carnetconduite.ui.home

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.UiEvent
import com.florent.carnetconduite.ui.home.screens.*
import kotlinx.coroutines.flow.collect
import org.koin.androidx.compose.koinViewModel

/**
 * Écran principal Home - gère l'affichage selon l'état de conduite
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val drivingState by viewModel.drivingState.collectAsState()
    val context = LocalContext.current

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
                    // Géré par l'écran spécifique si nécessaire
                }
            }
        }
    }

    // Récupérer les trajets pour les passer aux écrans
    val trips by viewModel.repository.getAllTrips().collectAsState(initial = emptyList())

    // Trouver le trajet actif selon l'état
    val currentTrip = when (drivingState) {
        DrivingState.OUTWARD_ACTIVE -> trips.find { it.endKm == null && !it.isReturn }
        DrivingState.ARRIVED -> trips.find { !it.isReturn && it.endKm != null &&
                trips.none { return@none it.pairedTripId == trips.find { !it.isReturn }?.id && it.isReturn } }
        DrivingState.RETURN_READY -> trips.find { it.isReturn && it.endKm == null }
        DrivingState.RETURN_ACTIVE -> trips.find { it.isReturn && it.endKm == null }
        else -> null
    }

    // Affichage conditionnel selon l'état
    when (drivingState) {
        DrivingState.IDLE -> {
            IdleScreen(viewModel = viewModel)
        }

        DrivingState.OUTWARD_ACTIVE -> {
            currentTrip?.let { trip ->
                OutwardActiveScreen(trip = trip, viewModel = viewModel)
            }
        }

        DrivingState.ARRIVED -> {
            currentTrip?.let { trip ->
                ArrivedScreen(trip = trip, viewModel = viewModel)
            }
        }

        DrivingState.RETURN_READY -> {
            currentTrip?.let { trip ->
                ReturnReadyScreen(trip = trip, viewModel = viewModel)
            }
        }

        DrivingState.RETURN_ACTIVE -> {
            currentTrip?.let { trip ->
                ReturnActiveScreen(trip = trip, viewModel = viewModel)
            }
        }

        DrivingState.COMPLETED -> {
            CompletedScreen(viewModel = viewModel)
        }
    }
}