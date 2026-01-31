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
import com.florent.carnetconduite.ui.home.components.TripHeaderCompact
import com.florent.carnetconduite.ui.home.components.TripSummaryEdit
import com.florent.carnetconduite.ui.home.components.TripSummaryHeader
import com.florent.carnetconduite.ui.home.components.TripSummaryVariant
import com.florent.carnetconduite.ui.home.mapper.colorsForState
import com.florent.carnetconduite.ui.home.mapper.findTripForState
import com.florent.carnetconduite.ui.home.mapper.headerForState
import com.florent.carnetconduite.ui.home.sections.ArrivedDecisionDialogs
import com.florent.carnetconduite.ui.home.sections.ArrivedDecisionPrimaryAction
import com.florent.carnetconduite.ui.home.sections.CompletedSummaryPrimaryAction
import com.florent.carnetconduite.ui.home.sections.IdleFormPrimaryAction
import com.florent.carnetconduite.ui.home.sections.OutwardActiveFormDialogs
import com.florent.carnetconduite.ui.home.sections.OutwardActiveFormPrimaryAction
import com.florent.carnetconduite.ui.home.sections.ReturnActiveFormDialogs
import com.florent.carnetconduite.ui.home.sections.ReturnActiveFormPrimaryAction
import com.florent.carnetconduite.ui.home.sections.ReturnReadyFormPrimaryAction
import com.florent.carnetconduite.util.formatTime
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

        val reservedBottom = 84.dp + 170.dp

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ----- CONTENU : juste le form -----
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
                    val colors = colorsForState(state)

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
                                tripGroups = tripGroups
                            )
                        }
                    }
                }
            }

            // ----- BOTTOM BAR : header + summary + CTA -----
            val header = headerForState(drivingState)
            val colors = colorsForState(drivingState)
            val showActiveIndicator =
                drivingState == DrivingState.OUTWARD_ACTIVE || drivingState == DrivingState.RETURN_ACTIVE

            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TripHeaderCompact(
                        header = header,
                        statusColor = colors.statusColor,
                        containerColor = colors.headerContainer,
                        onContainerColor = colors.onHeaderContainer,
                        showActiveIndicator = showActiveIndicator
                    )

                    if (headerTrip != null && drivingState != DrivingState.COMPLETED) {
                        TripSummaryHeader(
                            trip = headerTrip,
                            variant = TripSummaryVariant.Sticky,
                            statusLabel = header.statusLabel,
                            statusColor = colors.statusColor,
                            showDistance = false,
                            edit = editForState(
                                state = drivingState,
                                trip = headerTrip,
                                ui = ui
                            )
                        )
                    }

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

            // ----- DIALOGS -----
            OutwardActiveFormDialogs(outwardTrip, ui.outward, viewModel)
            ReturnActiveFormDialogs(returnActiveTrip, ui.returnActive, viewModel)
            ArrivedDecisionDialogs(arrivedTrip, ui.arrived, viewModel)
        }
    }
}

/**
 * ✅ Un seul champ éditable pertinent.
 * ⚠️ Remplace les 3 lignes "ui.xxx.____ = true" par TES noms exacts.
 */
@Composable
private fun editForState(
    state: DrivingState,
    trip: com.florent.carnetconduite.data.Trip,
    ui: HomeUnifiedState
): TripSummaryEdit? {
    return when (state) {
        DrivingState.OUTWARD_ACTIVE -> {
            // Exemple : éditer heure d’arrivée prévue / réelle (selon ton UX)
            TripSummaryEdit(
                label = "Heure d’arrivée",
                value = if ((trip.endTime ?: 0L) > 0L) formatTime(trip.endTime!!) else "—",
                onEdit = {
                    // TODO: remplace par ton flag réel
                    // ui.outward.showEditEndTime = true
                }
            )
        }

        DrivingState.ARRIVED -> {
            TripSummaryEdit(
                label = "Km d’arrivée",
                value = (trip.endKm?.toString() ?: "—"),
                onEdit = {
                    // TODO: remplace par ton flag réel
                    // ui.arrived.showEditEndKm = true
                }
            )
        }

        DrivingState.RETURN_ACTIVE -> {
            TripSummaryEdit(
                label = "Heure d’arrivée",
                value = if ((trip.endTime ?: 0L) > 0L) formatTime(trip.endTime!!) else "—",
                onEdit = {
                    // TODO: remplace par ton flag réel
                    // ui.returnActive.showEditEndTime = true
                }
            )
        }

        else -> null
    }
}
