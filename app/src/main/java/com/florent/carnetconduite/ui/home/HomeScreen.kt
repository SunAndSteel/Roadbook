package com.florent.carnetconduite.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
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
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.ui.UiEvent
import com.florent.carnetconduite.ui.home.components.StickyBottomArea
import com.florent.carnetconduite.ui.home.components.TripHeaderCompact
import com.florent.carnetconduite.ui.home.components.TripSummaryHeader
import com.florent.carnetconduite.ui.home.components.TripSummaryVariant
import com.florent.carnetconduite.ui.home.mapper.colorsForDrivingState
import com.florent.carnetconduite.ui.home.mapper.headerForDrivingState
import com.florent.carnetconduite.ui.home.sections.ArrivedDecisionDialogs
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

    // État UI unifié (inclut les champs d'arrivée et les flags de dialogues).
    val ui = rememberHomeUiState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ShowConfirmDialog -> Unit
            }
        }
    }

    val tripSnapshot = remember(trips) { HomeTripSnapshot.fromTrips(trips) }
    val headerTrip = tripSnapshot.headerTrip(drivingState)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // -------- CONTENU SCROLLABLE --------
            HomeScrollableContent(
                drivingState = drivingState,
                headerTrip = headerTrip,
                ui = ui,
                tripSnapshot = tripSnapshot,
                tripGroups = tripGroups,
                scrollState = scrollState
            )

            // -------- STICKY BOTTOM UNIQUE --------
            StickyBottomArea(
                drivingState = drivingState,
                tripSnapshot = tripSnapshot,
                ui = ui,
                viewModel = viewModel,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // -------- DIALOGS --------
            HomeDialogs(
                tripSnapshot = tripSnapshot,
                ui = ui,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HomeScrollableContent(
    drivingState: DrivingState,
    headerTrip: Trip?,
    ui: HomeUiState,
    tripSnapshot: HomeTripSnapshot,
    tripGroups: List<TripGroup>,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = reservedBottomPadding(drivingState)),
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
            val header = headerForDrivingState(state)
            val colors = colorsForDrivingState(state)
            val showActiveIndicator =
                state == DrivingState.OUTWARD_ACTIVE || state == DrivingState.RETURN_ACTIVE

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Entête principal (statut unique).
                TripHeaderCompact(
                    header,
                    colors.statusColor,
                    colors.headerContainer,
                    colors.onHeaderContainer,
                    showActiveIndicator
                )

                // Résumé compact sans duplication du statut.
                if (headerTrip != null && state != DrivingState.COMPLETED) {
                    TripSummaryHeader(
                        trip = headerTrip,
                        variant = TripSummaryVariant.Minimal,
                        statusLabel = header.statusLabel,
                        statusColor = colors.statusColor,
                        showStatusChip = false   // <-- plus de double actif
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
                            tripSnapshot = tripSnapshot,
                            tripGroups = tripGroups,
                            // Les champs d'arrivée restent dans la zone sticky.
                            showArrivalInputsInForm = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeDialogs(
    tripSnapshot: HomeTripSnapshot,
    ui: HomeUiState,
    viewModel: HomeViewModel
) {
    OutwardActiveFormDialogs(tripSnapshot.outward, ui.outward, viewModel)
    ReturnActiveFormDialogs(tripSnapshot.returnActive, ui.returnActive, viewModel)
    ArrivedDecisionDialogs(tripSnapshot.arrived, ui.arrival, viewModel)
}

private fun reservedBottomPadding(drivingState: DrivingState) = 80.dp + when (drivingState) {
    DrivingState.ARRIVED -> 230.dp
    else -> 120.dp
}
