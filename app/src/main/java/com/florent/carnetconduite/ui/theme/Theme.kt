package com.florent.carnetconduite.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext

// --- MODE DE THÈME ---
enum class ThemeMode {
    DYNAMIC,
    LIGHT,
    DARK
}

// --- PALETTE MODERNE ---

// 🎨 LIGHT MODE - Palette vibrante et moderne
val BrandRed = Color(0xFFDD042B)

private val LightColors = lightColorScheme(
    // Primary: Ton Rouge Marque (#dd042b)
    primary = BrandRed,
    onPrimary = Color(0xFFFFFFFF),
    // Un rouge très pâle pour les conteneurs (fonds de boutons activés, sélections)
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410006), // Rouge très foncé pour le texte sur fond pâle

    // Secondary: Bleu Nuit/Slate (Plus pro et moderne que le vert émeraude avec du rouge)
    secondary = Color(0xFF1E293B),          // Slate-800
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2E8F0), // Slate-200
    onSecondaryContainer = Color(0xFF0F172A), // Slate-900

    // Tertiary: Sarcelle/Teal (Offre un contraste frais avec le rouge)
    tertiary = Color(0xFF0D9488),           // Teal-600
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCCFBF1),  // Teal-100
    onTertiaryContainer = Color(0xFF115E59), // Teal-800

    // Error: On garde un rouge standard pour les erreurs, ou on utilise une variante
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410006),

    // Background & Surface: Blanc cassé "Zinc" (plus chaud que le gris pur)
    background = Color(0xFFF9FAFB),         // Zinc-50/Gray-50
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE4E4E7),     // Zinc-200 (Bordures douces, cartes grises)
    onSurfaceVariant = Color(0xFF44474E),

    // Outline: Gris subtils
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6CF)
)

// 🌙 DARK MODE - Profond et Vibrant
private val DarkColors = darkColorScheme(
    // Primary: On éclaircit le rouge pour le Dark Mode (lisibilité)
    // mais on garde l'esprit vif.
    primary = Color(0xFFFF546E),            // Un rouge/corail lumineux
    onPrimary = Color(0xFF3B0007),          // Texte foncé sur le rouge
    primaryContainer = BrandRed,            // On utilise ton rouge exact ici pour les gros blocs
    onPrimaryContainer = Color(0xFFFFDAD6), // Texte clair sur ton rouge

    // Secondary: Slate bleu clair
    secondary = Color(0xFF94A3B8),          // Slate-400
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF334155), // Slate-700
    onSecondaryContainer = Color(0xFFE2E8F0),

    // Tertiary: Teal lumineux
    tertiary = Color(0xFF5EEAD4),           // Teal-300
    onTertiary = Color(0xFF003833),
    tertiaryContainer = Color(0xFF005049),
    onTertiaryContainer = Color(0xFF99F6E4),

    // Error
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Background: Très sombre ("OLED Friendly" mais avec une teinte)
    background = Color(0xFF09090B),         // Zinc-950 (Presque noir, très moderne)
    onBackground = Color(0xFFE2E2E6),

    surface = Color(0xFF18181B),            // Zinc-900
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF27272A),     // Zinc-800
    onSurfaceVariant = Color(0xFFC4C6CF),

    // Outline
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E)
)

// --- THEME PRINCIPAL ---
@Composable
fun CarnetConduiteTheme(
    themeMode: ThemeMode = ThemeMode.DYNAMIC,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val isDarkTheme = isSystemInDarkTheme()

    val colorScheme = when (themeMode) {
        ThemeMode.DYNAMIC -> {
            if (isDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        ThemeMode.LIGHT -> LightColors
        ThemeMode.DARK -> DarkColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun themeIcon(themeMode: ThemeMode): ImageVector = when (themeMode) {
    ThemeMode.DYNAMIC -> Icons.Default.AutoAwesome
    ThemeMode.LIGHT -> Icons.Default.LightMode
    ThemeMode.DARK -> Icons.Default.DarkMode
}