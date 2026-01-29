package com.florent.carnetconduite.ui.settings

import androidx.compose.runtime.Composable
import com.florent.carnetconduite.domain.models.UserSettings
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme
import com.florent.carnetconduite.ui.theme.ThemeMode

@DevicePreview
@Composable
fun SettingsScreenPreview() {
    RoadbookTheme {
        SettingsScreen(
            settings = UserSettings(
                themeMode = ThemeMode.DARK,
                defaultGuide = "2",
                showDeleteConfirmations = true
            ),
            onThemeModeChange = {},
            onDefaultGuideChange = {},
            onShowDeleteConfirmationsChange = {},
            onResetToDefaults = {},
            onNavigateBack = {}
        )
    }
}
