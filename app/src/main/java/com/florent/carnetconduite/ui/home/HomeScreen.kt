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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.UiEvent
import com.florent.carnetconduite.ui.home.components.StickyBottomArea
import com.florent.carnetconduite.ui.home.components.TripSummaryHeader
import com.florent.carnetconduite.ui.home.components.TripSummaryVariant
import com.florent.carnetconduite.ui.home.mapper.colorsForState
import com.florent.carnetconduite.ui.home.mapper.findTripForState
import com.florent.carnetconduite.ui.home.mapper.headerForState
import com.florent.carnetconduite.ui.home.sections.ArrivedDecisionState
import com.florent.carnetconduite.ui.home.sections.OutwardActiveFormDialogs
import com.florent.carnetconduite.ui.home.sections.ReturnActiveFormDialogs
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

    // ✅ état UI unifié (contient aussi les inputs “arrivée” maintenant)
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

    val outwardTrip = findTripForState(DrivingState.OUTWARD_ACTIVE, trips)
    val arrivedTrip = findTripForState(DrivingState.ARRIVED, trips)
    val returnReadyTrip = findTripForState(DrivingState.RETURN_READY, trips)
    val returnActiveTrip = findTripForState(DrivingState.RETURN_ACTIVE, trips)

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

        // Bottom nav (~80dp) + sticky (card arrivée + CTA)
        val reservedBottom = 80.dp + when (drivingState) {
            DrivingState.ARRIVED -> 230.dp
            else -> 120.dp
        }

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
                        fadeIn() + slideInVertically { it / 10 } togetherWith
                                fadeOut() + slideOutVertically { -it / 10 }
                    },
                    label = "HomeUnifiedTransition"
                ) { state ->
                    val header = headerForState(state)
                    val colors = colorsForState(state)
                    val showActiveIndicator =
                        state == DrivingState.OUTWARD_ACTIVE || state == DrivingState.RETURN_ACTIVE

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // ✅ TripHeader en haut (statut = un seul endroit)
                        TripHeader(
                            header,
                            colors.statusColor,
                            colors.headerContainer,
                            colors.onHeaderContainer,
                            showActiveIndicator
                        )

                        // ✅ Résumé en haut, sans “Actif” dupliqué
                        if (headerTrip != null && state != DrivingState.COMPLETED) {
                            TripSummaryHeader(
                                trip = headerTrip,
                                variant = TripSummaryVariant.Minimal,
                                statusLabel = header.statusLabel,
                                statusColor = colors.statusColor,
                                showStatusChip = false,   // <-- plus de double actif
                                showEditButton = false    // <-- plus de “heure d’arrivée” près du CTA
                            )
                        }

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
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
                                    tripGroups = tripGroups,
                                    // ✅ Les inputs “arrivée” ne doivent PLUS être dans le form
                                    showArrivalInputsInForm = false
                                )
                            }
                        }
                    }
                }
            }

            // -------- STICKY BOTTOM UNIQUE --------
            StickyBottomArea(
                drivingState = drivingState,
                outwardTrip = outwardTrip,
                arrivedTrip = arrivedTrip,
                returnReadyTrip = returnReadyTrip,
                returnActiveTrip = returnActiveTrip,
                ui = ui,
                viewModel = viewModel,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // -------- DIALOGS --------
            OutwardActiveFormDialogs(outwardTrip, ui.outward, viewModel)
            ReturnActiveFormDialogs(returnActiveTrip, ui.returnActive, viewModel)
            ArrivedDecisionDialogs(arrivedTrip, ui.arrived, viewModel)
        }
    }
}

@Composable
fun ArrivedDecisionDialogs(x0: Trip?, x1: ArrivedDecisionState, x2: HomeViewModel) {
    TODO("Not yet implemented")
}
