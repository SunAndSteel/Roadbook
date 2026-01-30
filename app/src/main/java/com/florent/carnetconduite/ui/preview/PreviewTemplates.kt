package com.florent.carnetconduite.ui.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.florent.carnetconduite.ui.theme.CarnetConduiteTheme

@Preview(name = "Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class DevicePreview

@Composable
fun RoadbookTheme(content: @Composable () -> Unit) {
    CarnetConduiteTheme(content = content)
}
