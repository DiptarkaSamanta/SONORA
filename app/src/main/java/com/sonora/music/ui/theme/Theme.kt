package com.sonora.music.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SonoraPurple,
    onPrimary = SonoraTextPrimary,
    primaryContainer = SonoraPurpleDark,
    secondary = SonoraElectricBlue,
    onSecondary = SonoraBlack,
    tertiary = SonoraMagenta,
    background = SonoraBlack,
    onBackground = SonoraTextPrimary,
    surface = SonoraCharcoal,
    onSurface = SonoraTextPrimary,
    surfaceVariant = SonoraCardBg,
    onSurfaceVariant = SonoraTextSecondary
)

@Composable
fun SonoraTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = SonoraBlack.toArgb()
            window.navigationBarColor = SonoraBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

