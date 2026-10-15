package com.aldiandrew.neobrutallauncher

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
internal fun SettingsHomeSection(
    uiSurface: Color,
    uiOnSurface: Color,
    isDark: Boolean,
    appTileContentMode: TileContentMode,
    onAppTileContentModeChange: (TileContentMode) -> Unit,
    favoritesCount: Int,
    homeAppCount: Int,
    onHomeAppCountChange: (Int) -> Unit,
    onOpenPinnedAppPicker: () -> Unit,
    onClearFavorites: () -> Unit,
    swipeDownNotifications: Boolean,
    onSwipeDownNotificationsChange: (Boolean) -> Unit,
    homeClockDoubleTapEnabled: Boolean,
    onHomeClockDoubleTapChange: (Boolean) -> Unit,
    homeClockLongPressEnabled: Boolean,
    onHomeClockLongPressChange: (Boolean) -> Unit
) {
        SettingsSectionTitle("Home screen")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("App tile content", fontSize = 13.sp, fontWeight = FontWeight.Black)
                Text("The selected content mode adapts to every tile size.", fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(TileContentMode.ICON, TileContentMode.ICON_TEXT, TileContentMode.TEXT).forEach { mode ->
                        SettingsOptionButton(when(mode) { TileContentMode.ICON -> "ICON"; TileContentMode.ICON_TEXT -> "ICON + TEXT"; TileContentMode.TEXT -> "TEXT" }, appTileContentMode == mode, when(mode) { TileContentMode.ICON -> BrutalColors.Cyan; TileContentMode.ICON_TEXT -> BrutalColors.Yellow; TileContentMode.TEXT -> BrutalColors.Pink }, Modifier.weight(1f)) { onAppTileContentModeChange(mode) }
                    }
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
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(
                    "$favoritesCount PINNED APPS",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = uiOnSurface
                )
                Text(
                    "Choose how many Home app tiles are available.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = uiOnSurface.copy(alpha = .75f)
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 5).forEach { count ->
                        SettingsOptionButton(
                            count.toString(),
                            homeAppCount == count,
                            if (count == 3) BrutalColors.Cyan
                            else if (count == 5) BrutalColors.Yellow
                            else BrutalColors.Pink,
                            Modifier.weight(1f)
                        ) {
                            onHomeAppCountChange(count)
                        }
                    }
                }
                SettingsActionButton(
                    "CHOOSE PINNED APPS",
                    BrutalColors.Cyan,
                    Modifier.fillMaxWidth()
                ) {
                    onOpenPinnedAppPicker()
                }
                SettingsActionButton(
                    "CLEAR ALL PINNED APPS",
                    BrutalColors.Yellow,
                    onClick = onClearFavorites
                )
            }
        }

        SettingsSectionTitle("GESTURES")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsSwitch("SWIPE DOWN TO OPEN NOTIFICATIONS", "Swipe down on Home to open the notification panel.", swipeDownNotifications, BrutalColors.Cyan, onSwipeDownNotificationsChange)
                SettingsSwitch("DOUBLE-TAP CLOCK → SETTINGS", "Open Settings by double-tapping the Home clock.", homeClockDoubleTapEnabled, BrutalColors.Yellow, onHomeClockDoubleTapChange)
                SettingsSwitch("LONG-PRESS CLOCK → APPS", "Open the Apps page by long-pressing the Home clock.", homeClockLongPressEnabled, BrutalColors.Pink, onHomeClockLongPressChange)
                Text("Permissions needed: none.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = uiOnSurface.copy(alpha = .7f))
            }
        }

}

@Composable
internal fun SettingsIconSection(
    uiSurface: Color,
    uiOnSurface: Color,
    isDark: Boolean,
    selectedIconPack: IconPackInfo?,
    onOpenIconPackPicker: () -> Unit
) {
        SettingsSectionTitle("Icon")
        BrutalBlock(
            Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 2.dp,
            shadowX = 0.dp,
            shadowY = 0.dp,
            shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = selectedIconPack?.label?.uppercase(Locale.ENGLISH) ?: "SYSTEM ICONS",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = uiOnSurface
                )
                Text(
                    text = "Choose the icon pack used by Home and Apps.",
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = uiOnSurface.copy(alpha = .75f)
                )
                SettingsActionButton(
                    title = "CHOOSE ICON PACK",
                    background = BrutalColors.Yellow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    onOpenIconPackPicker()
                }
            }
        }


}

@Composable
internal fun SettingsLiveSection(
    uiBackground: Color,
    uiSurface: Color,
    uiOnSurface: Color,
    isDark: Boolean,
    showWeather: Boolean,
    onShowWeatherChange: (Boolean) -> Unit,
    locationPermissionGranted: Boolean,
    onRequestWeatherPermission: () -> Unit,
    notificationAccessGranted: Boolean,
    onOpenNotificationAccess: () -> Unit,
    selectedChatApp: AppInfo?,
    onOpenChatAppPicker: () -> Unit,
    onChatNotificationPackagesChange: (List<String>) -> Unit
) {
        SettingsSectionTitle("Weather")
        SettingsSwitch("WEATHER", "Show current weather on Home and Live. Location permission is required.", showWeather, BrutalColors.Cyan, onShowWeatherChange)
        if (!locationPermissionGranted) SettingsActionButton("Allow location access", BrutalColors.Yellow, onClick = onRequestWeatherPermission)

        SettingsSectionTitle("INTEGRATIONS")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Music and notification access", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.Normal, color = if (uiBackground == BrutalColors.DarkPaper) BrutalColors.DarkWhite else BrutalColors.Ink)
                Text(if (notificationAccessGranted) "Notification access is enabled for music playback and Live chat." else "Allow notification access to show music playback and recent Live chat notifications.", fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal, color = uiOnSurface.copy(alpha = .75f))
                SettingsActionButton(if (notificationAccessGranted) "OPEN NOTIFICATION ACCESS" else "ALLOW MUSIC / NOTIFICATION ACCESS", if (notificationAccessGranted) BrutalColors.Yellow else BrutalColors.Yellow, onClick = onOpenNotificationAccess)
            }
        }
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Live chat tile", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(selectedChatApp?.label?.uppercase(Locale.ENGLISH) ?: "NO APP SELECTED", fontSize = 13.sp, fontWeight = FontWeight.Black)
                Text("The latest notification from this app appears on LIVE.", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsActionButton(if (selectedChatApp == null) "CHOOSE APP" else "CHANGE APP", BrutalColors.Yellow, Modifier.weight(1f)) { onOpenChatAppPicker() }
                    if (selectedChatApp != null) SettingsActionButton("CLEAR", BrutalColors.White, Modifier.weight(.7f)) { onChatNotificationPackagesChange(emptyList()) }
                }
            }
        }


}
