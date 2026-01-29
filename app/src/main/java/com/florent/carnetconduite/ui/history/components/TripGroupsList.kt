package com.florent.carnetconduite.ui.history.components

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
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.ui.history.dialogs.EditTripGroupDialog
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

@Composable
fun TripGroupsList(
    tripGroups: List<TripGroup>,
    onDelete: (TripGroup) -> Unit,
    onEditStartTime: (Trip, Long) -> Unit,
    onEditEndTime: (Trip, Long) -> Unit,
    onEditStartKm: (Trip, Int) -> Unit,
    onEditEndKm: (Trip, Int) -> Unit,
    onEditDate: (Trip, String) -> Unit,
    onEditConditions: (Trip, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGroupForEdit by remember { mutableStateOf<TripGroup?>(null) }

    if (tripGroups.isEmpty()) {
        EmptyTripGroupsState(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        onDelete = { onDelete(tripGroup) },
                        modifier = Modifier.animateContentSize()
                    )
                }
            }
        }
    }

    // Dialog d'édition
    selectedGroupForEdit?.let { group ->
        EditTripGroupDialog(
            group = group,
            onDismiss = { selectedGroupForEdit = null },
            onEditStartTime = onEditStartTime,
            onEditEndTime = onEditEndTime,
            onEditStartKm = onEditStartKm,
            onEditEndKm = onEditEndKm,
            onEditDate = onEditDate,
            onEditConditions = onEditConditions
        )
    }
}

@Composable
private fun EmptyTripGroupsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
                    imageVector = Icons.Rounded.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Aucun trajet terminé",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Vos trajets terminés apparaîtront ici",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.7f)
                )
            }
        }
    }
}

private val previewTripGroups = listOf(
    TripGroup(
        outward = Trip(
            id = 201L,
            startKm = 9800,
            endKm = 10040,
            startPlace = "Bordeaux - Centre",
            endPlace = "Mérignac",
            startTime = 1704445200000L,
            endTime = 1704448800000L,
            isReturn = false,
            pairedTripId = 201L,
            status = TripStatus.COMPLETED,
            conditions = "Clair",
            guide = "1",
            date = "2024-01-10"
        ),
        returnTrip = Trip(
            id = 202L,
            startKm = 10040,
            endKm = 10260,
            startPlace = "Mérignac",
            endPlace = "Bordeaux - Centre",
            startTime = 1704450600000L,
            endTime = 1704454200000L,
            isReturn = true,
            pairedTripId = 201L,
            status = TripStatus.COMPLETED,
            conditions = "Clair",
            guide = "1",
            date = "2024-01-10"
        ),
        seanceNumber = 7
    )
)

@DevicePreview
@Composable
private fun TripGroupsListEmptyPreview() {
    RoadbookTheme {
        TripGroupsList(
            tripGroups = emptyList(),
            onDelete = {},
            onEditStartTime = { _, _ -> },
            onEditEndTime = { _, _ -> },
            onEditStartKm = { _, _ -> },
            onEditEndKm = { _, _ -> },
            onEditDate = { _, _ -> },
            onEditConditions = { _, _ -> }
        )
    }
}

@DevicePreview
@Composable
private fun TripGroupsListPreview() {
    RoadbookTheme {
        TripGroupsList(
            tripGroups = previewTripGroups,
            onDelete = {},
            onEditStartTime = { _, _ -> },
            onEditEndTime = { _, _ -> },
            onEditStartKm = { _, _ -> },
            onEditEndKm = { _, _ -> },
            onEditDate = { _, _ -> },
            onEditConditions = { _, _ -> }
        )
    }
}
