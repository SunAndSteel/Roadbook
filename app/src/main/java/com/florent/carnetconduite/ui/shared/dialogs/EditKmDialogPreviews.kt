package com.florent.carnetconduite.ui.shared.dialogs

import androidx.compose.runtime.Composable
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

@DevicePreview
@Composable
private fun EditKmDialogPreview() {
    RoadbookTheme {
        EditKmDialog(
            title = "Modifier km arrivée",
            initialKm = 12620,
            onDismiss = {},
            onConfirm = {}
        )
    }
}
