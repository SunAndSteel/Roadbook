package com.florent.carnetconduite.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Structure layout for Home screens: top status, summary header, main body, optional stats.
 * Keeps the order consistent across states (see UI contract).
 */
@Composable
internal fun HomeTripScaffold(
    topStatus: (@Composable () -> Unit)?,
    summary: (@Composable () -> Unit)?,
    body: @Composable () -> Unit,
    stats: (@Composable () -> Unit)?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        topStatus?.invoke()
        summary?.invoke()
        body()
        stats?.invoke()
    }
}
