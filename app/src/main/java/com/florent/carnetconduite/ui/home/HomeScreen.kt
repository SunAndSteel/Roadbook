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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.ui.UiEvent
import com.florent.carnetconduite.ui.home.components.PrimaryActionArea
import com.florent.carnetconduite.ui.home.components.TripHeader
import com.florent.carnetconduite.ui.home.components.TripSummaryHeader
import com.florent.carnetconduite.ui.home.components.TripSummaryVariant
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
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val drivingState by viewModel.drivingState.collectAsState()
    val trips by viewModel.trips.collectAsState()
    val tripGroups by viewModel.tripGroups.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // ✅ Un seul “bundle” d’état UI pour tous les modes
    val ui = rememberHomeUnifiedState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ShowConfirmDialog -> Unit
            }
        }
    }

    // ✅ On calcule les trips utiles UNE FOIS (pas de “currentTrip” ambigu)
    val outwardTrip = findTripForState(DrivingState.OUTWARD_ACTIVE, trips)
    val arrivedTrip = findTripForState(DrivingState.ARRIVED, trips)
    val returnReadyTrip = findTripForState(DrivingState.RETURN_READY, trips)
    val returnActiveTrip = findTripForState(DrivingState.RETURN_ACTIVE, trips)

    // “trip principal” pour le résumé en haut
    val headerTrip = when (drivingState) {
        DrivingState.OUTWARD_ACTIVE -> outwardTrip
        DrivingState.ARRIVED -> arrivedTrip
        DrivingState.RETURN_READY -> returnReadyTrip
        DrivingState.RETURN_ACTIVE -> returnActiveTrip
        else -> null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        // Réserve safe : bottom nav (~80dp) + barre sticky (~96dp)
        val reservedBottom = 80.dp + 96.dp

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // -------- CONTENU SCROLLABLE --------
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = reservedBottom),
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
                    val header = headerForState(state)
                    val colors = colorsForState(state)
                    val showActiveIndicator =
                        state == DrivingState.OUTWARD_ACTIVE || state == DrivingState.RETURN_ACTIVE

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TripHeader(
                            header,
                            colors.statusColor,
                            colors.headerContainer,
                            colors.onHeaderContainer,
                            showActiveIndicator
                        )

                        // Résumé seulement quand un trip existe et pas en Completed
                        if (headerTrip != null && state != DrivingState.COMPLETED) {
                            TripSummaryHeader(
                                headerTrip,
                                TripSummaryVariant.Minimal,
                                header.statusLabel,
                                colors.statusColor,
                                false
                            )
                        }

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = colors.cardContainer),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // ✅ Un seul écran : on swap juste le bloc “form”
                                when (state) {
                                    DrivingState.IDLE -> {
                                        IdleScreenContent(ui.idle)
                                    }

                                    DrivingState.OUTWARD_ACTIVE -> {
                                        // OutwardActive content ne dépend pas de trip (par design actuel)
                                        OutwardActiveScreenContent(ui.outward)
                                    }

                                    DrivingState.ARRIVED -> {
                                        // Arrived content ne dépend pas de trip (par design actuel)
                                        ArrivedScreenContent()
                                    }

                                    DrivingState.RETURN_READY -> {
                                        returnReadyTrip?.let { trip ->
                                            ReturnReadyScreenContent(trip, ui.returnReady)
                                        }
                                    }

                                    DrivingState.RETURN_ACTIVE -> {
                                        // ReturnActive content ne dépend pas de trip (par design actuel)
                                        ReturnActiveScreenContent(ui.returnActive)
                                    }

                                    DrivingState.COMPLETED -> {
                                        // ✅ On évite la dépendance au VM : on passe la data
                                        CompletedScreenContent(tripGroups)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -------- BARRE D’ACTION STICKY --------
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrimaryActionArea {
                        when (drivingState) {
                            DrivingState.IDLE ->
                                IdleScreenPrimaryAction(ui.idle, viewModel)

                            DrivingState.OUTWARD_ACTIVE ->
                                outwardTrip?.let { trip ->
                                    OutwardActiveScreenPrimaryAction(trip, ui.outward, viewModel)
                                }

                            DrivingState.ARRIVED ->
                                arrivedTrip?.let { trip ->
                                    ArrivedScreenPrimaryAction(trip, viewModel)
                                }

                            DrivingState.RETURN_READY ->
                                returnReadyTrip?.let { trip ->
                                    ReturnReadyScreenPrimaryAction(trip, ui.returnReady, viewModel)
                                }

                            DrivingState.RETURN_ACTIVE ->
                                returnActiveTrip?.let { trip ->
                                    ReturnActiveScreenPrimaryAction(trip, ui.returnActive, viewModel)
                                }

                            DrivingState.COMPLETED ->
                                CompletedScreenPrimaryAction()
                        }
                    }
                }
            }

            // -------- DIALOGS (overlay) --------
            OutwardActiveScreenDialogs(outwardTrip, ui.outward, viewModel)
            ReturnActiveScreenDialogs(returnActiveTrip, ui.returnActive, viewModel)
            ArrivedScreenDialogs(arrivedTrip, ui.arrived, viewModel)
        }
    }
}
