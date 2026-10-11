package com.aldiandrew.neobrutallauncher

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.material3.LocalTextStyle

object BrutalColors {
    var activePreset by mutableStateOf(DesignPreset.NEO_BRUTAL_CLASSIC)
        private set
    var darkAppearance by mutableStateOf(false)
        private set
    fun configure(preset: DesignPreset, isDark: Boolean) {
        activePreset = preset
        darkAppearance = isDark
    }
    val Ink = Color(0xFF111111)
    val White = Color(0xFFFFFFFF)
    val DarkWhite = Color(0xFFFFFFFF)
    val DarkPaper = Color(0xFF121212)
    val Paper: Color get() = if (darkAppearance) DarkPaper else when (activePreset) {
        DesignPreset.NEO_BRUTAL_CLASSIC -> Color(0xFFF4F0E6)
        DesignPreset.VIOLET_GRID -> Color(0xFFEDE5FF)
    }
    val Yellow: Color get() = when (activePreset) {
        DesignPreset.NEO_BRUTAL_CLASSIC -> Color(0xFFFFE500)
        DesignPreset.VIOLET_GRID -> Color(0xFFB69CFF)
    }
    val Pink: Color get() = when (activePreset) {
        DesignPreset.NEO_BRUTAL_CLASSIC -> Color(0xFFFF5C8A)
        DesignPreset.VIOLET_GRID -> Color(0xFF9D4EDD)
    }
    val Cyan: Color get() = when (activePreset) {
        DesignPreset.NEO_BRUTAL_CLASSIC -> Color(0xFF00D9FF)
        DesignPreset.VIOLET_GRID -> Color(0xFF6C63FF)
    }
    val DarkTile: Color get() = when (activePreset) {
        DesignPreset.NEO_BRUTAL_CLASSIC -> Color(0xFF202020)
        DesignPreset.VIOLET_GRID -> Color(0xFF21172E)
    }
    val Red: Color get() = when (activePreset) {
        DesignPreset.NEO_BRUTAL_CLASSIC -> Color(0xFFE00000)
        DesignPreset.VIOLET_GRID -> Color(0xFF6930C3)
    }
    val Lime: Color get() = when (activePreset) {
        DesignPreset.NEO_BRUTAL_CLASSIC -> Color(0xFFB8FF9F)
        DesignPreset.VIOLET_GRID -> Color(0xFFD0BFFF)
    }
    fun appPalette(seed: Int = 0): List<Color> {
        val base = when (activePreset) {
            DesignPreset.NEO_BRUTAL_CLASSIC -> listOf(White, Yellow, Pink, Cyan)
            DesignPreset.VIOLET_GRID -> listOf(Cyan, Pink, White, Yellow)
        }
        val shift = Math.floorMod(seed, base.size)
        return List(base.size) { index -> base[(index + shift) % base.size] }
    }
}

private fun lightScheme() = lightColorScheme(
    primary = BrutalColors.Ink,
    onPrimary = BrutalColors.White,
    secondary = BrutalColors.Pink,
    onSecondary = BrutalColors.Ink,
    background = BrutalColors.Paper,
    onBackground = BrutalColors.Ink,
    surface = BrutalColors.Paper,
    onSurface = BrutalColors.Ink,
    surfaceVariant = BrutalColors.White,
    onSurfaceVariant = BrutalColors.Ink,
    outline = BrutalColors.Ink,
    outlineVariant = BrutalColors.Ink.copy(alpha = 0.55f),
    error = BrutalColors.Red,
    onError = BrutalColors.White
)

private fun darkScheme() = darkColorScheme(
    primary = BrutalColors.DarkWhite,
    onPrimary = BrutalColors.Ink,
    secondary = BrutalColors.Lime,
    onSecondary = BrutalColors.Ink,
    background = BrutalColors.DarkPaper,
    onBackground = BrutalColors.DarkWhite,
    surface = BrutalColors.DarkTile,
    onSurface = BrutalColors.DarkWhite,
    surfaceVariant = Color(0xFF303030),
    onSurfaceVariant = BrutalColors.DarkWhite,
    outline = BrutalColors.DarkWhite,
    outlineVariant = Color(0xFF707070),
    error = BrutalColors.Red,
    onError = BrutalColors.White
)

@Composable
fun NeoBrutalTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    designPreset: DesignPreset = DesignPreset.NEO_BRUTAL_CLASSIC,
    typographyStyle: TypographyStyle = TypographyStyle.DEFAULT,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val view = LocalView.current

    SideEffect {
        BrutalColors.configure(designPreset, isDark)
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowInsetsControllerCompat(window, view)
        controller.isAppearanceLightStatusBars = !isDark
        controller.isAppearanceLightNavigationBars = !isDark
    }

    CompositionLocalProvider(
        LocalBrutalMetrics provides BrutalMetrics(),
        LocalBrutalTypographyStyle provides typographyStyle
    ) {
        MaterialTheme(colorScheme = if (isDark) darkScheme() else lightScheme()) {
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(
                    fontFamily = BrutalTypography.Body
                )
            ) {
                content()
            }
        }
    }
}
