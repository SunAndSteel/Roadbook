package com.florent.carnetconduite.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.KeyboardReturn
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.UTurnLeft
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.ui.UiEvent
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
import com.florent.carnetconduite.ui.home.screens.IdleScreenState
import com.florent.carnetconduite.ui.home.screens.rememberIdleScreenState
import com.florent.carnetconduite.ui.home.screens.rememberOutwardActiveScreenState
import com.florent.carnetconduite.ui.home.screens.rememberReturnActiveScreenState
import com.florent.carnetconduite.ui.home.screens.rememberReturnReadyScreenState
import kotlinx.coroutines.flow.collect
import org.koin.androidx.compose.koinViewModel
import com.florent.carnetconduite.util.formatTimeRange

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

private data class TripHeaderData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val statusLabel: String
)

private data class StepColors(
    val headerContainer: Color,
    val onHeaderContainer: Color,
    val statusColor: Color,
    val cardContainer: Color
)

private fun headerForState(state: DrivingState): TripHeaderData {
    return when (state) {
        DrivingState.IDLE -> TripHeaderData(
            icon = Icons.Rounded.DirectionsCar,
            title = "Prêt à partir",
            subtitle = "Nouveau trajet",
            statusLabel = "Prêt"
        )
        DrivingState.OUTWARD_ACTIVE -> TripHeaderData(
            icon = Icons.Rounded.DirectionsCar,
            title = "Trajet en cours",
            subtitle = "Trajet aller",
            statusLabel = "Actif"
        )
        DrivingState.ARRIVED -> TripHeaderData(
            icon = Icons.Rounded.Flag,
            title = "Arrivée confirmée",
            subtitle = "Trajet aller terminé",
            statusLabel = "Décision"
        )
        DrivingState.RETURN_READY -> TripHeaderData(
            icon = Icons.Rounded.UTurnLeft,
            title = "Retour prêt",
            subtitle = "Aller-retour",
            statusLabel = "Prêt"
        )
        DrivingState.RETURN_ACTIVE -> TripHeaderData(
            icon = Icons.Rounded.KeyboardReturn,
            title = "Retour en cours",
            subtitle = "Trajet retour",
            statusLabel = "Actif"
        )
        DrivingState.COMPLETED -> TripHeaderData(
            icon = Icons.Rounded.CheckCircle,
            title = "Trajets sauvegardés",
            subtitle = "Session terminée",
            statusLabel = "Terminé"
        )
    }
}

@Composable
private fun TripHeader(
    header: TripHeaderData,
    statusColor: Color,
    containerColor: Color,
    onContainerColor: Color,
    showActiveIndicator: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = statusColor,
                    modifier = Modifier
                        .height(56.dp)
                        .width(56.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = header.icon,
                            contentDescription = null,
                            tint = contentColorFor(statusColor),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = header.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = onContainerColor
                    )
                    Text(
                        text = header.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onContainerColor,
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            }

            AssistChip(
                onClick = {},
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (showActiveIndicator) {
                            PulsingDot(
                                color = statusColor,
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(8.dp)
                            )
                        }
                        Text(
                            text = header.statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = statusColor.copy(alpha = 0.15f),
                    labelColor = statusColor
                )
            )
        }
    }
}

@Composable
private fun TripSummary(
    trip: Trip?,
    stateLabel: String,
    accentColor: Color,
    showIdleSetup: Boolean,
    idleState: IdleScreenState?
) {
    var expanded by remember { mutableStateOf(true) }
    val title = if (showIdleSetup) "Préparation rapide" else "Résumé du trajet"
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    onClick = { expanded = !expanded },
                    label = {
                        Text(
                            text = if (expanded) "Réduire" else "Détails",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = accentColor.copy(alpha = 0.12f),
                        labelColor = accentColor
                    )
                )
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                if (trip == null && showIdleSetup && idleState != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Configure rapidement le départ avant de lancer le trajet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = idleState.startKm,
                            onValueChange = { idleState.startKm = it },
                            label = { Text("Kilométrage départ") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Speed,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        OutlinedTextField(
                            value = idleState.startPlace,
                            onValueChange = { idleState.startPlace = it },
                            label = { Text("Lieu de départ") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                } else if (trip != null) {
                    ListItem(
                        headlineContent = {
                            Text("${trip.startPlace} → ${trip.endPlace ?: "—"}")
                        },
                        supportingContent = { Text("Lieux") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.Route,
                                contentDescription = null,
                                tint = accentColor
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    ListItem(
                        headlineContent = {
                            Text(formatTripTime(trip))
                        },
                        supportingContent = { Text("Horaires") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = accentColor
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    ListItem(
                        headlineContent = {
                            Text("${trip.nbKmsParcours} km")
                        },
                        supportingContent = { Text("Kilomètres") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.DirectionsCar,
                                contentDescription = null,
                                tint = accentColor
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                } else {
                    Text(
                        text = "Ajoute un départ pour lancer un nouveau trajet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PulsingDot(
    color: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "ActiveIndicator")
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ActiveIndicatorScale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ActiveIndicatorAlpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun PrimaryActionArea(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
private fun colorsForState(state: DrivingState): StepColors {
    val scheme = MaterialTheme.colorScheme
    return when (state) {
        DrivingState.IDLE -> StepColors(
            headerContainer = scheme.primaryContainer,
            onHeaderContainer = scheme.onPrimaryContainer,
            statusColor = scheme.primary,
            cardContainer = scheme.surface
        )
        DrivingState.OUTWARD_ACTIVE -> StepColors(
            headerContainer = scheme.secondaryContainer,
            onHeaderContainer = scheme.onSecondaryContainer,
            statusColor = scheme.secondary,
            cardContainer = scheme.surface
        )
        DrivingState.ARRIVED -> StepColors(
            headerContainer = scheme.secondaryContainer,
            onHeaderContainer = scheme.onSecondaryContainer,
            statusColor = scheme.secondary,
            cardContainer = scheme.surface
        )
        DrivingState.RETURN_READY -> StepColors(
            headerContainer = scheme.primaryContainer,
            onHeaderContainer = scheme.onPrimaryContainer,
            statusColor = scheme.primary,
            cardContainer = scheme.surface
        )
        DrivingState.RETURN_ACTIVE -> StepColors(
            headerContainer = scheme.tertiaryContainer,
            onHeaderContainer = scheme.onTertiaryContainer,
            statusColor = scheme.tertiary,
            cardContainer = scheme.surface
        )
        DrivingState.COMPLETED -> StepColors(
            headerContainer = scheme.surfaceVariant,
            onHeaderContainer = scheme.onSurfaceVariant,
            statusColor = scheme.primary,
            cardContainer = scheme.surface
        )
    }
}

private fun findTripForState(state: DrivingState, trips: List<Trip>): Trip? {
    return when (state) {
        DrivingState.OUTWARD_ACTIVE -> trips.find {
            it.endKm == null && !it.isReturn && it.status == TripStatus.ACTIVE
        }
        DrivingState.ARRIVED -> trips.find {
            !it.isReturn && it.endKm != null &&
                trips.none { return@none it.pairedTripId == trips.find { !it.isReturn }?.id && it.isReturn }
        }
        DrivingState.RETURN_READY -> trips.find { it.isReturn && it.status == TripStatus.READY }
        DrivingState.RETURN_ACTIVE -> trips.find {
            it.isReturn && it.endKm == null && it.status == TripStatus.ACTIVE
        }
        else -> null
    }
}

private fun formatTripTime(trip: Trip): String {
    return formatTimeRange(trip.startTime, trip.endTime, ongoingLabel = "En cours")
}
