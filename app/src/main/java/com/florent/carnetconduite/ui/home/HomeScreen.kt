package com.florent.carnetconduite.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.florent.carnetconduite.ui.home.components.HomeTripScaffold
import com.florent.carnetconduite.ui.home.components.PrimaryActionArea
import com.florent.carnetconduite.ui.home.components.TripHeader
import com.florent.carnetconduite.ui.home.components.TripSummaryHeader
import com.florent.carnetconduite.ui.home.components.TripSummaryVariant
import com.florent.carnetconduite.ui.home.mapper.colorsForState
import com.florent.carnetconduite.ui.home.mapper.findTripForState
import com.florent.carnetconduite.ui.home.mapper.headerForState
import com.florent.carnetconduite.ui.home.screens.ArrivedStatsSection
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
 * Écran principal Home - gère l'affichage selon l'état de conduite.
 * Sections respect the UI contract order: top status, summary header, body, optional stats.
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

    val currentTripForAction = findTripForState(drivingState, trips)
    val primaryAction: (@Composable () -> Unit)? = when (drivingState) {
        DrivingState.IDLE -> {
            {
                PrimaryActionArea {
                    IdleScreenPrimaryAction(state = idleState, viewModel = viewModel)
                }
            }
        }
        DrivingState.OUTWARD_ACTIVE -> {
            currentTripForAction?.let { trip ->
                {
                    PrimaryActionArea {
                        OutwardActiveScreenPrimaryAction(
                            trip = trip,
                            state = outwardState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
        DrivingState.ARRIVED -> {
            currentTripForAction?.let { trip ->
                {
                    PrimaryActionArea {
                        ArrivedScreenPrimaryAction(
                            trip = trip,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
        DrivingState.RETURN_READY -> {
            currentTripForAction?.let { trip ->
                {
                    PrimaryActionArea {
                        ReturnReadyScreenPrimaryAction(
                            trip = trip,
                            state = returnReadyState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
        DrivingState.RETURN_ACTIVE -> {
            currentTripForAction?.let { trip ->
                {
                    PrimaryActionArea {
                        ReturnActiveScreenPrimaryAction(
                            trip = trip,
                            state = returnActiveState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
        DrivingState.COMPLETED -> {
            {
                PrimaryActionArea {
                    CompletedScreenPrimaryAction()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            primaryAction?.let { action ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    action()
                }
            }
        }
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
                val summaryStatusLabel = if (state == DrivingState.ARRIVED) {
                    "Terminé"
                } else {
                    header.statusLabel
                }

                HomeTripScaffold(
                    topStatus = {
                        TripHeader(
                            header = header,
                            statusColor = colors.statusColor,
                            containerColor = colors.headerContainer,
                            onContainerColor = colors.onHeaderContainer,
                            showActiveIndicator = state == DrivingState.OUTWARD_ACTIVE || state == DrivingState.RETURN_ACTIVE
                        )
                    },
                    summary = currentTrip?.let { trip ->
                        when (state) {
                            DrivingState.IDLE,
                            DrivingState.COMPLETED -> null
                            else -> {
                                {
                                    TripSummaryHeader(
                                        trip = trip,
                                        variant = when (state) {
                                            DrivingState.ARRIVED -> TripSummaryVariant.Minimal
                                            DrivingState.RETURN_READY -> TripSummaryVariant.Compact
                                            else -> TripSummaryVariant.Expanded
                                        },
                                        statusLabel = summaryStatusLabel,
                                        accentColor = colors.statusColor,
                                        showDistance = state != DrivingState.ARRIVED,
                                        onEditStartTime = when (state) {
                                            DrivingState.OUTWARD_ACTIVE -> {
                                                { outwardState.showEditStartTime = true }
                                            }
                                            DrivingState.RETURN_ACTIVE -> {
                                                { returnActiveState.showEditStartTime = true }
                                            }
                                            else -> null
                                        },
                                        onEditEndTime = when (state) {
                                            DrivingState.OUTWARD_ACTIVE -> {
                                                { outwardState.showEditEndTime = true }
                                            }
                                            DrivingState.RETURN_ACTIVE -> {
                                                { returnActiveState.showEditEndTime = true }
                                            }
                                            DrivingState.ARRIVED -> {
                                                { arrivedState.showEditEndTime = true }
                                            }
                                            else -> null
                                        },
                                        prioritizeEndTime = state == DrivingState.OUTWARD_ACTIVE || state == DrivingState.RETURN_ACTIVE,
                                        onEditDistance = null
                                    )
                                }
                            }
                        }
                    },
                    body = {
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
                        }
                    },
                    stats = currentTrip?.let { trip ->
                        when (state) {
                            DrivingState.ARRIVED -> {
                                {
                                    ArrivedStatsSection(
                                        trip = trip,
                                        onEditDistance = { arrivedState.showEditEndKm = true }
                                    )
                                }
                            }
                            else -> null
                        }
                    }
                )
            }

            OutwardActiveScreenDialogs(trip = findTripForState(DrivingState.OUTWARD_ACTIVE, trips), state = outwardState, viewModel = viewModel)
            ReturnActiveScreenDialogs(trip = findTripForState(DrivingState.RETURN_ACTIVE, trips), state = returnActiveState, viewModel = viewModel)
            ArrivedScreenDialogs(trip = findTripForState(DrivingState.ARRIVED, trips), state = arrivedState, viewModel = viewModel)
        }
    }
}
