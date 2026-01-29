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
import com.florent.carnetconduite.ui.history.dialogs.EditTripGroupDialog

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
