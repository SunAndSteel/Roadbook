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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Scaffold
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
import com.florent.carnetconduite.ui.home.components.PrimaryActionArea
import com.florent.carnetconduite.ui.home.components.TripHeader
import com.florent.carnetconduite.ui.home.components.TripSummary
import com.florent.carnetconduite.ui.home.mapper.colorsForState
import com.florent.carnetconduite.ui.home.mapper.findTripForState
import com.florent.carnetconduite.ui.home.mapper.headerForState
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenContent
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenDialogs
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.CompletedScreenContent
import com.florent.carnetconduite.ui.home.screens.CompletedScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.IdleScreenContent
import com.florent.carnetconduite.ui.home.screens.IdleScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenDialogs
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenDialogs
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.rememberArrivedScreenState
import com.florent.carnetconduite.ui.home.screens.rememberIdleScreenState
import com.florent.carnetconduite.ui.home.screens.rememberOutwardActiveScreenState
import com.florent.carnetconduite.ui.home.screens.rememberReturnActiveScreenState
import com.florent.carnetconduite.ui.home.screens.rememberReturnReadyScreenState
import kotlinx.coroutines.flow.collect
import org.koin.androidx.compose.koinViewModel

/**
 * Écran principal Home - gère l'affichage selon l'état de conduite
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                val header = headerForState(state)
                val colors = colorsForState(state)

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TripHeader(
                        header = header,
                        statusColor = colors.statusColor,
                        containerColor = colors.headerContainer,
                        onContainerColor = colors.onHeaderContainer,
                        showActiveIndicator = state == DrivingState.OUTWARD_ACTIVE || state == DrivingState.RETURN_ACTIVE
                    )
                    TripSummary(
                        trip = currentTrip,
                        stateLabel = header.subtitle,
                        accentColor = colors.statusColor,
                        showIdleSetup = state == DrivingState.IDLE,
                        idleState = idleState
                    )

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = colors.cardContainer
                        ),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = 6.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (state) {
                                DrivingState.IDLE -> {
                                    IdleScreenContent(state = idleState)
                                }
                                DrivingState.OUTWARD_ACTIVE -> {
                                    currentTrip?.let {
                                        OutwardActiveScreenContent(trip = it, state = outwardState)
                                    }
                                }
                                DrivingState.ARRIVED -> {
                                    currentTrip?.let {
                                        ArrivedScreenContent(trip = it, state = arrivedState)
                                    }
                                }
                                DrivingState.RETURN_READY -> {
                                    currentTrip?.let {
                                        ReturnReadyScreenContent(trip = it, state = returnReadyState)
                                    }
                                }
                                DrivingState.RETURN_ACTIVE -> {
                                    currentTrip?.let {
                                        ReturnActiveScreenContent(trip = it, state = returnActiveState)
                                    }
                                }
                                DrivingState.COMPLETED -> {
                                    CompletedScreenContent(viewModel = viewModel)
                                }
                            }
                        }
                    }

                    PrimaryActionArea {
                        when (state) {
                            DrivingState.IDLE -> {
                                IdleScreenPrimaryAction(state = idleState, viewModel = viewModel)
                            }
                            DrivingState.OUTWARD_ACTIVE -> {
                                currentTrip?.let {
                                    OutwardActiveScreenPrimaryAction(
                                        trip = it,
                                        state = outwardState,
                                        viewModel = viewModel
                                    )
                                }
                            }
                            DrivingState.ARRIVED -> {
                                currentTrip?.let {
                                    ArrivedScreenPrimaryAction(
                                        trip = it,
                                        viewModel = viewModel
                                    )
                                }
                            }
                            DrivingState.RETURN_READY -> {
                                currentTrip?.let {
                                    ReturnReadyScreenPrimaryAction(
                                        trip = it,
                                        state = returnReadyState,
                                        viewModel = viewModel
                                    )
                                }
                            }
                            DrivingState.RETURN_ACTIVE -> {
                                currentTrip?.let {
                                    ReturnActiveScreenPrimaryAction(
                                        trip = it,
                                        state = returnActiveState,
                                        viewModel = viewModel
                                    )
                                }
                            }
                            DrivingState.COMPLETED -> {
                                CompletedScreenPrimaryAction()
                            }
                        }
                    }
                }
            }

            OutwardActiveScreenDialogs(trip = findTripForState(DrivingState.OUTWARD_ACTIVE, trips), state = outwardState, viewModel = viewModel)
            ReturnActiveScreenDialogs(trip = findTripForState(DrivingState.RETURN_ACTIVE, trips), state = returnActiveState, viewModel = viewModel)
            ArrivedScreenDialogs(trip = findTripForState(DrivingState.ARRIVED, trips), state = arrivedState, viewModel = viewModel)
        }
    }
}
