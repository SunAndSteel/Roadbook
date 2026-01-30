package com.florent.carnetconduite.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.ui.UiEvent
import com.florent.carnetconduite.ui.home.components.PrimaryActionCard
import com.florent.carnetconduite.ui.home.components.PrimaryActionInfoLine
import com.florent.carnetconduite.ui.home.mapper.colorsForState
import com.florent.carnetconduite.ui.home.mapper.findTripForState
import com.florent.carnetconduite.ui.home.screens.ArrivedStatsSection
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenContent
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenDialogs
import com.florent.carnetconduite.ui.home.screens.CompletedScreenContent
import com.florent.carnetconduite.ui.home.screens.IdleScreenContent
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenDialogs
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenDialogs
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenContent
import com.florent.carnetconduite.ui.home.screens.rememberArrivedScreenState
import com.florent.carnetconduite.ui.home.screens.rememberIdleScreenState
import com.florent.carnetconduite.ui.home.screens.rememberOutwardActiveScreenState
import com.florent.carnetconduite.ui.home.screens.rememberReturnActiveScreenState
import com.florent.carnetconduite.ui.home.screens.rememberReturnReadyScreenState
import com.florent.carnetconduite.util.formatTime
import com.florent.carnetconduite.util.formatTimeRange
import kotlinx.coroutines.flow.collect
import org.koin.androidx.compose.koinViewModel

/**
 * Écran principal Home - gère l'affichage selon l'état de conduite.
 * La PrimaryActionCard est la source de vérité de l'état et des actions.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val drivingState by viewModel.drivingState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.ShowConfirmDialog -> {
                    // Géré par l'écran spécifique si nécessaire
                }
            }
        }
    }

    // Récupérer les trajets pour les passer aux écrans
    val trips by viewModel.trips.collectAsState()
    val scrollState = rememberScrollState()

    val idleState = rememberIdleScreenState()
    val outwardState = rememberOutwardActiveScreenState()
    val arrivedState = rememberArrivedScreenState()
    val returnReadyState = rememberReturnReadyScreenState()
    val returnActiveState = rememberReturnActiveScreenState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedContent(
                targetState = drivingState,
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 6 } togetherWith
                        fadeOut() + slideOutVertically { -it / 6 }
                },
                label = "HomeStepTransition"
            ) { state ->
                val currentTrip = findTripForState(state, trips)
                val colors = colorsForState(state)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (state) {
                            DrivingState.IDLE -> {
                                IdleScreenContent(state = idleState)
                            }
                            DrivingState.OUTWARD_ACTIVE -> {
                                currentTrip?.let {
                                    OutwardActiveScreenContent(state = outwardState)
                                }
                            }
                            DrivingState.ARRIVED -> {
                                currentTrip?.let {
                                    ArrivedScreenContent()
                                }
                            }
                            DrivingState.RETURN_READY -> {
                                currentTrip?.let {
                                    ReturnReadyScreenContent(trip = it, state = returnReadyState)
                                }
                            }
                            DrivingState.RETURN_ACTIVE -> {
                                currentTrip?.let {
                                    ReturnActiveScreenContent(state = returnActiveState)
                                }
                            }
                            DrivingState.COMPLETED -> {
                                CompletedScreenContent(viewModel = viewModel)
                            }
                        }
                    }

                    currentTrip?.let { trip ->
                        if (state == DrivingState.ARRIVED) {
                            ArrivedStatsSection(
                                trip = trip,
                                onEditDistance = { arrivedState.showEditEndKm = true }
                            )
                        }
                    }

                    PrimaryActionCard(
                        title = primaryCardTitle(state),
                        subtitle = primaryCardSubtitle(state, currentTrip),
                        icon = primaryCardIcon(state),
                        infoLines = primaryCardInfoLines(
                            state = state,
                            trip = currentTrip,
                            outwardState = outwardState,
                            arrivedState = arrivedState,
                            returnActiveState = returnActiveState
                        ),
                        actionLabel = primaryCardActionLabel(state),
                        onAction = {
                            when (state) {
                                DrivingState.IDLE -> {
                                    viewModel.startOutward(
                                        startKm = idleState.startKm.toIntOrNull() ?: 0,
                                        startPlace = idleState.startPlace,
                                        conditions = idleState.conditions,
                                        guide = idleState.guide
                                    )
                                }
                                DrivingState.OUTWARD_ACTIVE -> {
                                    currentTrip?.let { trip ->
                                        viewModel.finishOutward(
                                            tripId = trip.id,
                                            endKm = outwardState.endKm.toIntOrNull() ?: 0,
                                            endPlace = outwardState.endPlace
                                        )
                                    }
                                }
                                DrivingState.ARRIVED -> {
                                    arrivedState.showDecisionDialog = true
                                }
                                DrivingState.RETURN_READY -> {
                                    returnReadyState.showDecisionDialog = true
                                }
                                DrivingState.RETURN_ACTIVE -> {
                                    currentTrip?.let { trip ->
                                        viewModel.finishReturn(
                                            tripId = trip.id,
                                            endKm = returnActiveState.endKm.toIntOrNull() ?: 0
                                        )
                                    }
                                }
                                DrivingState.COMPLETED -> {
                                    // TODO: Hook into new-trip action when available
                                }
                            }
                        },
                        containerColor = colors.cardContainer,
                        iconContainerColor = MaterialTheme.colorScheme.surface,
                        onIconContainerColor = colors.statusColor,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            OutwardActiveScreenDialogs(trip = findTripForState(DrivingState.OUTWARD_ACTIVE, trips), state = outwardState, viewModel = viewModel)
            ReturnActiveScreenDialogs(trip = findTripForState(DrivingState.RETURN_ACTIVE, trips), state = returnActiveState, viewModel = viewModel)
            ArrivedScreenDialogs(trip = findTripForState(DrivingState.ARRIVED, trips), state = arrivedState, viewModel = viewModel)
            ReturnReadyDecisionDialog(
                state = returnReadyState,
                trip = findTripForState(DrivingState.RETURN_READY, trips),
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ReturnReadyDecisionDialog(
    state: com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenState,
    trip: com.florent.carnetconduite.data.Trip?,
    viewModel: HomeViewModel
) {
    if (trip == null || !state.showDecisionDialog) return
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { state.showDecisionDialog = false },
        title = { androidx.compose.material3.Text("Retour") },
        text = { androidx.compose.material3.Text("Que veux-tu faire pour le retour ?") },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    viewModel.startReturn(
                        returnTripId = trip.id,
                        actualStartKm = state.editedStartKm.toIntOrNull()
                    )
                    state.showDecisionDialog = false
                }
            ) {
                androidx.compose.material3.Text("Démarrer le retour")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    viewModel.cancelReturn(trip.id)
                    state.showDecisionDialog = false
                }
            ) {
                androidx.compose.material3.Text("Annuler le retour")
            }
        }
    )
}

private fun primaryCardTitle(state: DrivingState): String {
    return when (state) {
        DrivingState.IDLE -> "Prêt à partir"
        DrivingState.OUTWARD_ACTIVE -> "Trajet en cours"
        DrivingState.ARRIVED -> "Arrivée confirmée"
        DrivingState.RETURN_READY -> "Retour prêt"
        DrivingState.RETURN_ACTIVE -> "Trajet en cours"
        DrivingState.COMPLETED -> "Trajets sauvegardés"
    }
}

private fun primaryCardSubtitle(state: DrivingState, trip: com.florent.carnetconduite.data.Trip?): String {
    return when (state) {
        DrivingState.IDLE -> "Aucun trajet en cours"
        DrivingState.OUTWARD_ACTIVE -> trip?.let { "Depuis ${formatTime(it.startTime)}" } ?: "Depuis —"
        DrivingState.ARRIVED -> "Trajet aller terminé"
        DrivingState.RETURN_READY -> "Trajet retour prêt"
        DrivingState.RETURN_ACTIVE -> trip?.let { "Depuis ${formatTime(it.startTime)}" } ?: "Depuis —"
        DrivingState.COMPLETED -> "Session terminée"
    }
}

@Composable
private fun primaryCardIcon(state: DrivingState): androidx.compose.ui.graphics.vector.ImageVector {
    return when (state) {
        DrivingState.IDLE -> androidx.compose.material.icons.Icons.Rounded.DirectionsCar
        DrivingState.OUTWARD_ACTIVE -> androidx.compose.material.icons.Icons.Rounded.DirectionsCar
        DrivingState.ARRIVED -> androidx.compose.material.icons.Icons.Rounded.Flag
        DrivingState.RETURN_READY -> androidx.compose.material.icons.Icons.Rounded.UTurnLeft
        DrivingState.RETURN_ACTIVE -> androidx.compose.material.icons.Icons.Rounded.KeyboardReturn
        DrivingState.COMPLETED -> androidx.compose.material.icons.Icons.Rounded.CheckCircle
    }
}

@Composable
private fun primaryCardInfoLines(
    state: DrivingState,
    trip: com.florent.carnetconduite.data.Trip?,
    outwardState: com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenState,
    arrivedState: com.florent.carnetconduite.ui.home.screens.ArrivedScreenState,
    returnActiveState: com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenState
): List<PrimaryActionInfoLine> {
    if (trip == null) return emptyList()
    return when (state) {
        DrivingState.OUTWARD_ACTIVE -> listOf(
            PrimaryActionInfoLine(
                icon = androidx.compose.material.icons.Icons.Rounded.LocationOn,
                text = formatPlaces(
                    trip.startPlace,
                    outwardState.endPlace.takeIf { it.isNotBlank() } ?: trip.endPlace
                ),
                onEdit = { outwardState.showEditEndPlace = true }
            ),
            PrimaryActionInfoLine(
                icon = androidx.compose.material.icons.Icons.Rounded.Schedule,
                text = formatTime(trip.startTime),
                onEdit = { outwardState.showEditStartTime = true }
            )
        )
        DrivingState.ARRIVED -> listOf(
            PrimaryActionInfoLine(
                icon = androidx.compose.material.icons.Icons.Rounded.LocationOn,
                text = formatPlaces(trip.startPlace, trip.endPlace),
                onEdit = null
            ),
            PrimaryActionInfoLine(
                icon = androidx.compose.material.icons.Icons.Rounded.Schedule,
                text = formatTimeRange(trip.startTime, trip.endTime, ongoingLabel = "—"),
                onEdit = { arrivedState.showEditEndTime = true }
            )
        )
        DrivingState.RETURN_ACTIVE -> listOf(
            PrimaryActionInfoLine(
                icon = androidx.compose.material.icons.Icons.Rounded.LocationOn,
                text = formatPlaces(trip.startPlace, trip.endPlace),
                onEdit = null
            ),
            PrimaryActionInfoLine(
                icon = androidx.compose.material.icons.Icons.Rounded.Schedule,
                text = formatTime(trip.startTime),
                onEdit = { returnActiveState.showEditStartTime = true }
            )
        )
        DrivingState.RETURN_READY -> emptyList()
        else -> emptyList()
    }
}

private fun primaryCardActionLabel(state: DrivingState): String {
    return when (state) {
        DrivingState.IDLE -> "Démarrer"
        DrivingState.OUTWARD_ACTIVE -> "Arrivée"
        DrivingState.ARRIVED -> "Décision"
        DrivingState.RETURN_READY -> "Décision"
        DrivingState.RETURN_ACTIVE -> "Arrivée"
        DrivingState.COMPLETED -> "Nouveau trajet"
    }
}

private fun formatPlaces(startPlace: String?, endPlace: String?): String {
    val start = startPlace?.takeIf { it.isNotBlank() } ?: "—"
    val end = endPlace?.takeIf { it.isNotBlank() } ?: "—"
    return "$start → $end"
}
