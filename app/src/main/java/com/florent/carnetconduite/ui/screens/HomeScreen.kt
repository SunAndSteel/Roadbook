package com.florent.carnetconduite.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.ui.DrivingViewModel

@Composable
fun HomeScreen(viewModel: DrivingViewModel) {
    val state by viewModel.drivingState.collectAsState()
    val activeTrip by viewModel.activeTrip.collectAsState()
    val arrivedTrip by viewModel.arrivedTrip.collectAsState()
    val tripGroups by viewModel.tripGroups.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                (fadeIn() + slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialOffsetY = { it / 3 }
                )).togetherWith(
                    fadeOut() + slideOutVertically(
                        targetOffsetY = { -it / 3 }
                    )
                )
            },
            label = "state_transition"
        ) { currentState ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // En-tête avec indicateur d'état
                StateIndicatorCard(currentState)

                // Contenu selon l'état
                when (currentState) {
                    DrivingState.IDLE -> IdleScreen(viewModel = viewModel)

                    DrivingState.OUTWARD_ACTIVE -> activeTrip?.let { trip ->
                        OutwardActiveScreen(trip = trip, viewModel = viewModel)
                    }

                    DrivingState.ARRIVED -> arrivedTrip?.let { trip ->
                        ArrivedScreen(trip = trip, viewModel = viewModel)
                    }

                    DrivingState.RETURN_READY -> {
                        // Trouver le trajet retour en statut READY
                        tripGroups.flatMap { listOfNotNull(it.returnTrip) }
                            .firstOrNull { it.status == "READY" && it.isReturn }?.let { trip ->
                                ReturnReadyScreen(trip = trip, viewModel = viewModel)
                            }
                    }

                    DrivingState.RETURN_ACTIVE -> activeTrip?.let { trip ->
                        ReturnActiveScreen(trip = trip, viewModel = viewModel)
                    }

                    DrivingState.COMPLETED -> CompletedScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun StateIndicatorCard(state: DrivingState) {
    val (icon, title, color) = when (state) {
        DrivingState.IDLE -> Triple(
            Icons.Rounded.Home,
            "Prêt à partir",
            MaterialTheme.colorScheme.primaryContainer
        )
        DrivingState.OUTWARD_ACTIVE -> Triple(
            Icons.Rounded.DirectionsCar,
            "Trajet aller en cours",
            MaterialTheme.colorScheme.secondaryContainer
        )
        DrivingState.ARRIVED -> Triple(
            Icons.Rounded.LocationOn,
            "Arrivé à destination",
            MaterialTheme.colorScheme.tertiaryContainer
        )
        DrivingState.RETURN_READY -> Triple(
            Icons.Rounded.Schedule,
            "Prêt pour le retour",
            MaterialTheme.colorScheme.primaryContainer
        )
        DrivingState.RETURN_ACTIVE -> Triple(
            Icons.Rounded.KeyboardReturn,
            "Trajet retour en cours",
            MaterialTheme.colorScheme.secondaryContainer
        )
        DrivingState.COMPLETED -> Triple(
            Icons.Rounded.CheckCircle,
            "Trajet terminé",
            MaterialTheme.colorScheme.tertiaryContainer
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = color
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "État actuel",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.7f)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}