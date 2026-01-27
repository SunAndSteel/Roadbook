package com.florent.carnetconduite.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.ui.DrivingViewModel
import com.florent.carnetconduite.ui.components.StatsHeader
import com.florent.carnetconduite.ui.components.TripGroupCard
import com.florent.carnetconduite.ui.dialogs.EditTripGroupDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: DrivingViewModel) {
    val tripGroups by viewModel.tripGroups.collectAsState()
    val trips by viewModel.trips.collectAsState()
    var showEmptyState by remember { mutableStateOf(false) }
    var selectedGroupForEdit by remember { mutableStateOf<TripGroup?>(null) }

    // Calculer les statistiques localement
    val totalDistance = remember(trips) {
        trips.filter { it.status == "COMPLETED" }.sumOf { it.kmsComptabilises }
    }

    val totalDuration = remember(trips) {
        trips.filter { it.status == "COMPLETED" && it.endTime != null }
            .sumOf { (it.endTime!! - it.startTime) }
    }

    val tripCount = remember(tripGroups) {
        tripGroups.size
    }

    LaunchedEffect(tripGroups) {
        showEmptyState = tripGroups.isEmpty()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedVisibility(
            visible = showEmptyState,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            EmptyHistoryState()
        }

        AnimatedVisibility(
            visible = !showEmptyState,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // En-tête statistiques avec design moderne
                item {
                    StatsHeader(
                        totalDistance = totalDistance,
                        totalDuration = totalDuration,
                        tripCount = tripCount
                    )
                }

                // Section titre avec typographie Material 3
                item {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Historique des trajets",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${tripGroups.size} trajet${if (tripGroups.size > 1) "s" else ""} enregistré${if (tripGroups.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.alpha(0.7f)
                        )
                    }
                }

                // Liste des groupes de trajets avec animations
                items(
                    items = tripGroups,
                    key = { it.outward.id }
                ) { tripGroup ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        TripGroupCard(
                            tripGroup = tripGroup,
                            onEdit = { selectedGroupForEdit = tripGroup },
                            onDelete = { viewModel.deleteTripGroup(tripGroup) },
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }

                // Espacement final pour la navigation bar
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Dialog d'édition
    selectedGroupForEdit?.let { group ->
        EditTripGroupDialog(
            group = group,
            onDismiss = { selectedGroupForEdit = null },
            onEditStartTime = { trip, newTime ->
                viewModel.editStartTime(trip.id, newTime)
            },
            onEditEndTime = { trip, newTime ->
                viewModel.editEndTime(trip.id, newTime)
            },
            onEditStartKm = { trip, newKm ->
                viewModel.editStartKm(trip.id, newKm)
            },
            onEditEndKm = { trip, newKm ->
                viewModel.editEndKm(trip.id, newKm)
            }
        )
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Aucun trajet enregistré",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Commencez votre premier trajet depuis l'écran d'accueil",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.7f)
                )

                FilledTonalButton(
                    onClick = { /* Navigation handled by bottom bar */ },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Démarrer un trajet")
                }
            }
        }
    }
}