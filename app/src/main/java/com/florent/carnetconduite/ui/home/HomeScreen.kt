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
import com.florent.carnetconduite.ui.home.screens.ArrivedDecisionDialogs
import com.florent.carnetconduite.ui.home.screens.ArrivedDecisionPrimaryAction
import com.florent.carnetconduite.ui.home.screens.CompletedSummaryPrimaryAction
import com.florent.carnetconduite.ui.home.screens.IdleFormPrimaryAction
import com.florent.carnetconduite.ui.home.screens.OutwardActiveFormDialogs
import com.florent.carnetconduite.ui.home.screens.OutwardActiveFormPrimaryAction
import com.florent.carnetconduite.ui.home.screens.ReturnActiveFormDialogs
import com.florent.carnetconduite.ui.home.screens.ReturnActiveFormPrimaryAction
import com.florent.carnetconduite.ui.home.screens.ReturnReadyFormPrimaryAction
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
                                HomeFormSection(
                                    drivingState = state,
                                    ui = ui,
                                    outwardTrip = outwardTrip,
                                    arrivedTrip = arrivedTrip,
                                    returnReadyTrip = returnReadyTrip,
                                    returnActiveTrip = returnActiveTrip,
                                    tripGroups = tripGroups
                                )
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
                                IdleFormPrimaryAction(ui.idle, viewModel)

                            DrivingState.OUTWARD_ACTIVE ->
                                outwardTrip?.let { trip ->
                                    OutwardActiveFormPrimaryAction(trip, ui.outward, viewModel)
                                }

                            DrivingState.ARRIVED ->
                                arrivedTrip?.let { trip ->
                                    ArrivedDecisionPrimaryAction(trip, viewModel)
                                }

                            DrivingState.RETURN_READY ->
                                returnReadyTrip?.let { trip ->
                                    ReturnReadyFormPrimaryAction(trip, ui.returnReady, viewModel)
                                }

                            DrivingState.RETURN_ACTIVE ->
                                returnActiveTrip?.let { trip ->
                                    ReturnActiveFormPrimaryAction(trip, ui.returnActive, viewModel)
                                }

                            DrivingState.COMPLETED ->
                                CompletedSummaryPrimaryAction()
                        }
                    }
                }
            }

            // -------- DIALOGS (overlay) --------
            OutwardActiveFormDialogs(outwardTrip, ui.outward, viewModel)
            ReturnActiveFormDialogs(returnActiveTrip, ui.returnActive, viewModel)
            ArrivedDecisionDialogs(arrivedTrip, ui.arrived, viewModel)
        }
    }
}
