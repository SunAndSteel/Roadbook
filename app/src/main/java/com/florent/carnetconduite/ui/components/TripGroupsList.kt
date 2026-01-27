package com.florent.carnetconduite.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup

@Composable
fun TripGroupsList(
    tripGroups: List<TripGroup>,
    onDelete: (TripGroup) -> Unit,
    onEditStartTime: (Trip, Long) -> Unit,
    onEditEndTime: (Trip, Long) -> Unit,
    onEditStartKm: (Trip, Int) -> Unit,
    onEditEndKm: (Trip, Int) -> Unit
) {
    if (tripGroups.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Aucun trajet terminé",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Vos trajets apparaîtront ici",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tripGroups) { group ->
                TripGroupCard(
                    group = group,
                    onDelete = { onDelete(group) },
                    onEditStartTime = onEditStartTime,
                    onEditEndTime = onEditEndTime,
                    onEditStartKm = onEditStartKm,
                    onEditEndKm = onEditEndKm
                )
            }
        }
    }
}