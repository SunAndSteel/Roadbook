package com.florent.carnetconduite.ui.home.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.ui.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CompletedScreen(viewModel: HomeViewModel = koinViewModel()) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CompletedScreenContent(viewModel = viewModel)
    }
}

@Composable
fun CompletedScreenContent(viewModel: HomeViewModel) {
    val tripGroups by viewModel.tripGroups.collectAsState()
    CompletedScreenContent(tripGroups = tripGroups)
}

@Composable
fun CompletedScreenContent(tripGroups: List<TripGroup>) {
    val completedCount = tripGroups.count { group -> group.isComplete }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ton trajet est bien enregistré.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha(0.7f)
        )

        if (completedCount > 0) {
            Text(
                text = "Tu as complété $completedCount trajet${if (completedCount > 1) "s" else ""}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.6f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Choisis la suite depuis la carte principale.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
