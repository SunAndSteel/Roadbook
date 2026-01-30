package com.florent.carnetconduite.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.home.mapper.formatTripTime

/**
 * Variants for the TripSummaryHeader to avoid redundancy with stats and body content.
 */
enum class TripSummaryVariant {
    Expanded,
    Compact,
    Minimal
}

/**
 * State-driven summary header: places, times, and optional distance with inline edits.
 * Use [variant] to control density and redundancy with StatsSection.
 */
@Composable
internal fun TripSummaryHeader(
    trip: Trip,
    variant: TripSummaryVariant,
    statusLabel: String,
    accentColor: Color,
    showDistance: Boolean,
    onEditPlaces: (() -> Unit)? = null,
    onEditStartTime: (() -> Unit)? = null,
    onEditEndTime: (() -> Unit)? = null,
    prioritizeEndTime: Boolean = false,
    onEditDistance: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(variant == TripSummaryVariant.Expanded) }
    val isExpandable = variant != TripSummaryVariant.Minimal
    val placesLabel = formatPlaces(trip.startPlace, trip.endPlace)
    val timeLabel = formatTripTime(trip)
    val distanceLabel = formatDistance(trip.nbKmsParcours)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (variant != TripSummaryVariant.Minimal) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Résumé du trajet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isExpandable) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) {
                                    Icons.Rounded.KeyboardArrowUp
                                } else {
                                    Icons.Rounded.KeyboardArrowDown
                                },
                                contentDescription = if (expanded) {
                                    "Réduire"
                                } else {
                                    "Développer"
                                }
                            )
                        }
                    }
                }
            }

            ListItem(
                headlineContent = { Text(placesLabel) },
                supportingContent = { Text("Lieux") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Route,
                        contentDescription = null,
                        tint = accentColor
                    )
                },
                trailingContent = {
                    when {
                        variant == TripSummaryVariant.Minimal -> {
                            AssistChip(
                                onClick = {},
                                label = { Text(statusLabel) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = accentColor.copy(alpha = 0.12f),
                                    labelColor = accentColor
                                )
                            )
                        }
                        onEditPlaces != null -> {
                            IconButton(onClick = onEditPlaces) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Modifier les lieux"
                                )
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
            )

            if (variant != TripSummaryVariant.Minimal) {
                ListItem(
                    headlineContent = { Text(timeLabel) },
                    supportingContent = { Text("Horaires") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = accentColor
                        )
                    },
                    trailingContent = {
                        TripSummaryTimeActions(
                            onEditStartTime = onEditStartTime,
                            onEditEndTime = onEditEndTime,
                            prioritizeEndTime = prioritizeEndTime
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }

            AnimatedVisibility(visible = expanded && showDistance && variant != TripSummaryVariant.Minimal) {
                ListItem(
                    headlineContent = { Text(distanceLabel) },
                    supportingContent = { Text("Kilomètres") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsCar,
                            contentDescription = null,
                            tint = accentColor
                        )
                    },
                    trailingContent = {
                        onEditDistance?.let {
                            IconButton(onClick = it) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Modifier les kilomètres"
                                )
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        }
    }
}

@Composable
private fun TripSummaryTimeActions(
    onEditStartTime: (() -> Unit)?,
    onEditEndTime: (() -> Unit)?,
    prioritizeEndTime: Boolean
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val hasStart = onEditStartTime != null
    val hasEnd = onEditEndTime != null
    when {
        prioritizeEndTime && hasEnd -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditEndTime!!) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Modifier l'arrivée"
                    )
                }
                if (hasStart) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Autres options"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Modifier le départ") },
                                onClick = {
                                    onEditStartTime?.invoke()
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        hasStart -> {
            IconButton(onClick = { onEditStartTime?.invoke() }) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Modifier le départ"
                )
            }
        }
        hasEnd -> {
            IconButton(onClick = { onEditEndTime?.invoke() }) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Modifier l'arrivée"
                )
            }
        }
    }
}

private fun formatPlaces(startPlace: String?, endPlace: String?): String {
    val start = startPlace?.takeIf { it.isNotBlank() } ?: "—"
    val end = endPlace?.takeIf { it.isNotBlank() } ?: "—"
    return "$start → $end"
}

private fun formatDistance(distance: Int?): String {
    val safeDistance = distance ?: 0
    return if (safeDistance <= 0) "—" else "$safeDistance km"
}
