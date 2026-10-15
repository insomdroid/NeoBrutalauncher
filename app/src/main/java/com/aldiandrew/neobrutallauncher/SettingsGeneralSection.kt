package com.aldiandrew.neobrutallauncher

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
internal fun SettingsGeneralSection(
    context: android.content.Context,
    uiSurface: Color,
    uiOnSurface: Color,
    isDark: Boolean,
    themePreference: ThemePreference,
    onThemeChange: (ThemePreference) -> Unit,
    designPreset: DesignPreset,
    onDesignPresetChange: (DesignPreset) -> Unit,
    typographyStyle: TypographyStyle,
    onTypographyStyleChange: (TypographyStyle) -> Unit,
    wallpaperUri: String?,
    onChooseWallpaper: () -> Unit,
    onClearWallpaper: () -> Unit,
    use24Hour: Boolean,
    showAmPm: Boolean,
    onUse24HourChange: (Boolean) -> Unit,
    onShowAmPmChange: (Boolean) -> Unit,
    motionSmoothness: MotionSmoothness,
    onMotionSmoothnessChange: (MotionSmoothness) -> Unit,
    reduceMotion: Boolean,
    onReduceMotionChange: (Boolean) -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onOpenResetConfirm: () -> Unit
) {
        SettingsSectionTitle("APPEARANCE")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Theme", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = uiOnSurface)
                Text("Choose your theme mode.", fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal, color = uiOnSurface.copy(alpha = .75f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsOptionButton("SYSTEM", themePreference == ThemePreference.SYSTEM, BrutalColors.Cyan, Modifier.weight(1f)) { onThemeChange(ThemePreference.SYSTEM) }
                    SettingsOptionButton("LIGHT", themePreference == ThemePreference.LIGHT, BrutalColors.Yellow, Modifier.weight(1f)) { onThemeChange(ThemePreference.LIGHT) }
                    SettingsOptionButton("DARK", themePreference == ThemePreference.DARK, BrutalColors.Pink, Modifier.weight(1f)) { onThemeChange(ThemePreference.DARK) }
                }
            }
        }

        BrutalBlock(
            Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 2.dp,
            shadowX = 0.dp,
            shadowY = 0.dp,
            shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Color preset", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = uiOnSurface)
                Text("Choose the accent palette used across the launcher.", fontSize = 12.sp, lineHeight = 16.sp, color = uiOnSurface.copy(alpha = .75f))
                DesignPreset.values().toList().chunked(2).forEach { rowPresets ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowPresets.forEach { preset ->
                            val presetColor = when (preset) {
                                DesignPreset.NEO_BRUTAL_CLASSIC -> Color(0xFFFFE500)
                                DesignPreset.VIOLET_GRID -> Color(0xFFB69CFF)
                            }
                            SettingsOptionButton(
                                preset.label,
                                designPreset == preset,
                                presetColor,
                                Modifier.weight(1f).height(52.dp)
                            ) { onDesignPresetChange(preset) }
                        }
                    }
                }
            }
        }

        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Typography", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = uiOnSurface)
                Text("Choose the type style used throughout the launcher.", fontSize = 12.sp, lineHeight = 16.sp, color = uiOnSurface.copy(alpha = .75f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(TypographyStyle.DEFAULT, TypographyStyle.CONDENSED).forEach { style ->
                        SettingsOptionButton(
                            style.label,
                            typographyStyle == style,
                            BrutalColors.Cyan,
                            Modifier.weight(1f)
                        ) {
                            onTypographyStyleChange(style)
                        }
                    }
                }
            }
        }

        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Wallpaper", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = uiOnSurface)
                Text(
                    "Choose a background image for the Home screen.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = uiOnSurface.copy(alpha = .75f)
                )
                Text(
                    text = if (wallpaperUri == null) "Wallpaper" else "Wallpaper selected",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = uiOnSurface
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsActionButton(
                        "CHOOSE IMAGE",
                        BrutalColors.Pink,
                        Modifier.weight(1f),
                        onChooseWallpaper
                    )
                    SettingsActionButton(
                        "CLEAR",
                        BrutalColors.White,
                        Modifier.weight(0.7f),
                        onClearWallpaper
                    )
                }
            }
        }

        SettingsSectionTitle("Quotes")
        BrutalBlock(
            modifier = Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 2.dp,
            shadowX = 0.dp,
            shadowY = 0.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("Built-in quotes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = uiOnSurface)
                NeoQuotes.builtInQuotes().forEachIndexed { index, quote ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${index + 1}.",
                            modifier = Modifier.padding(end = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = uiOnSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = quote,
                            modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = uiOnSurface
                        )
                    }
                }
            }
        }

        SettingsSectionTitle("CLOCK")
        SettingsSwitch("24-HOUR TIME", "Use a 24-hour clock on Home and Live.", use24Hour, BrutalColors.Yellow, onUse24HourChange)
        if (!use24Hour) SettingsSwitch("AM / PM", "Show AM or PM when using the 12-hour clock on Home and Live.", showAmPm, BrutalColors.Cyan, onShowAmPmChange)
        SettingsSectionTitle("ANIMATION")
        BrutalBlock(
            Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 2.dp,
            shadowX = 0.dp,
            shadowY = 0.dp,
            shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Animation", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("Controls how quickly launcher movement settles.", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MotionSmoothness.values().forEach { smoothness ->
                        SettingsOptionButton(
                            label = smoothness.label,
                            selected = motionSmoothness == smoothness,
                            background = BrutalColors.Cyan,
                            modifier = Modifier.weight(1f),
                            onClick = { onMotionSmoothnessChange(smoothness) }
                        )
                    }
                }
            }
        }
        SettingsSwitch(
            "REDUCE MOTION",
            "Disable decorative launch and return movement.",
            reduceMotion,
            BrutalColors.Cyan,
            onReduceMotionChange
        )

        SettingsSectionTitle("DATA & RECOVERY")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Backup and restore", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = uiOnSurface)
                Text("Android automatic backup excludes launcher preferences, including notes/tasks. Use Backup and restore below to save or move your settings and private data manually; exported JSON may contain private text.", fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = uiOnSurface.copy(alpha = .75f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsActionButton("BACKUP", BrutalColors.Cyan, Modifier.weight(1f), onBackup)
                    SettingsActionButton("RESTORE", BrutalColors.Yellow, Modifier.weight(1f), onRestore)
                }
                SettingsActionButton("RESET ALL CUSTOMIZATIONS", BrutalColors.Pink) { onOpenResetConfirm() }
            }
        }

        SettingsSectionTitle("LAUNCHER")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Default Home app", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("Choose Neo Brutal Launcher as the Android default Home app.", fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
                SettingsActionButton("OPEN HOME SETTINGS", BrutalColors.Yellow) { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }
        }

        SettingsSectionTitle("ABOUT")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Neo Brutal Launcher", fontSize = 18.sp, fontWeight = FontWeight.Black, color = uiOnSurface)
                Text("Core build 0.1.0", fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = uiOnSurface.copy(alpha = .75f))
                Text("A neo-brutalist launcher focused on fast access to your apps.", fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, color = uiOnSurface)
            }
        }

}
