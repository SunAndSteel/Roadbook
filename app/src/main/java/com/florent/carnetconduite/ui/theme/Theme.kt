package com.florent.carnetconduite.ui.theme

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext

// --- MODE DE THÈME ---
enum class ThemeMode {
    Dynamic,
    Light,
    Dark
}

// --- PALETTE LIGHT ---
private val LightColors = lightColorScheme(
    primary = Color(0xFF5F6CAF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDEBFF),

    secondary = Color(0xFF52B788),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F1E7),

    tertiary = Color(0xFFFAA43A),
    onTertiary = Color.Black,

    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF)
)

// --- PALETTE DARK ---
private val DarkColors = darkColorScheme(
    primary = Color(0xFF8D90F0),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF2B2E6D),

    secondary = Color(0xFF38C28B),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1F4035),

    tertiary = Color(0xFFFFA751),
    onTertiary = Color.Black,

    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

// --- THEME PRINCIPAL ---
@Composable
fun CarnetConduiteTheme(
    themeMode: ThemeMode = ThemeMode.Dynamic,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when (themeMode) {

        ThemeMode.Dynamic -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Le système décide light/dark automatiquement
                dynamicLightColorScheme(context)
            } else {
                LightColors
            }
        }

        ThemeMode.Light -> LightColors
        ThemeMode.Dark -> DarkColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun themeIcon(themeMode: ThemeMode): ImageVector = when (themeMode) {
    ThemeMode.Dynamic -> Icons.Default.AutoAwesome
    ThemeMode.Light -> Icons.Default.LightMode
    ThemeMode.Dark -> Icons.Default.DarkMode
}
