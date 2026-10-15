package com.aldiandrew.neobrutallauncher

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch

@Composable
internal fun SettingsScreen(
    apps: List<AppInfo>,
    chatNotificationPackages: List<String>,
    themePreference: ThemePreference,
    designPreset: DesignPreset,
    use24Hour: Boolean,
    showAmPm: Boolean,
    homeAppCount: Int,
    showWeather: Boolean,
    appTileContentMode: TileContentMode,
    typographyStyle: TypographyStyle,
    iconPackPackage: String?,
    motionSmoothness: MotionSmoothness,
    reduceMotion: Boolean,
    settingsSwipeEnabled: Boolean,
    swipeDownNotifications: Boolean,
    homeClockDoubleTapEnabled: Boolean,
    homeClockLongPressEnabled: Boolean,
    wallpaperUri: String?,
    favorites: Set<String>,
    favoritesCount: Int,
    locationPermissionGranted: Boolean,
    notificationAccessGranted: Boolean,
    onChatNotificationPackagesChange: (List<String>) -> Unit,
    onBack: () -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    onDesignPresetChange: (DesignPreset) -> Unit,
    onUse24HourChange: (Boolean) -> Unit,
    onShowAmPmChange: (Boolean) -> Unit,
    onHomeAppCountChange: (Int) -> Unit,
    onShowWeatherChange: (Boolean) -> Unit,
    onRequestWeatherPermission: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onAppTileContentModeChange: (TileContentMode) -> Unit,
    onTypographyStyleChange: (TypographyStyle) -> Unit,
    onIconPackChange: (String?) -> Unit,
    onTogglePinnedApp: (AppInfo) -> Unit,
    onMotionSmoothnessChange: (MotionSmoothness) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onSettingsSwipeChange: (Boolean) -> Unit,
    onSwipeDownNotificationsChange: (Boolean) -> Unit,
    onHomeClockDoubleTapChange: (Boolean) -> Unit,
    onHomeClockLongPressChange: (Boolean) -> Unit,
    onChooseWallpaper: () -> Unit,
    onClearWallpaper: () -> Unit,
    onClearFavorites: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onResetAll: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("General") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiBackground = MaterialTheme.colorScheme.background
    val uiSurface = MaterialTheme.colorScheme.surface
    val uiOnSurface = MaterialTheme.colorScheme.onSurface
    val isDark = uiBackground == BrutalColors.DarkPaper
    val iconPacks = remember { IconPackManager(context).installedIconPacks() }
    val chatCandidates = remember(apps) {
        apps.groupBy { it.packageName }.values.mapNotNull { it.firstOrNull() }
            .filter { it.packageName != context.packageName }.sortedBy { it.label.lowercase() }
    }
    var showChatAppPicker by remember { mutableStateOf(false) }
    var showIconPackPicker by remember { mutableStateOf(false) }
    var showPinnedAppPicker by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    val selectedChatApp = chatCandidates.firstOrNull { it.packageName == chatNotificationPackages.firstOrNull() }
    val selectedIconPack = iconPacks.firstOrNull { it.packageName == iconPackPackage }
    val pinnedCandidates = remember(apps) {
        apps.groupBy { it.packageName }
            .values
            .mapNotNull { it.firstOrNull() }
            .filter { it.packageName != context.packageName }
            .sortedBy { it.label.lowercase() }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(uiBackground)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            .padding(
            horizontal = NeoBrutalTokens.Spacing.Medium,
            vertical = NeoBrutalTokens.Spacing.Medium
        )
            .verticalScroll(rememberScrollState())
            .pointerInput(selectedCategory) {
                val categories = listOf("General", "Home", "Icon", "Live")
                var dragTotal = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount ->
                        dragTotal += amount
                        if (abs(dragTotal) >= 80f) {
                            val current = categories.indexOf(selectedCategory).coerceAtLeast(0)
                            val next = (current + if (dragTotal < 0f) 1 else -1)
                                .coerceIn(0, categories.lastIndex)
                            selectedCategory = categories[next]
                            dragTotal = 0f
                        }
                    }
                )
            },
        verticalArrangement = Arrangement.spacedBy(NeoBrutalTokens.Spacing.Small)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = uiOnSurface) }
            BrutalBlock(modifier = Modifier.weight(1f), background = uiSurface, borderWidth = NeoBrutalTokens.Border.Primary, borderColor = uiOnSurface, shadowX = 3.dp, shadowY = 3.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
                Text("Settings", fontFamily = BrutalTypography.Display, fontSize = NeoBrutalTokens.Type.Title, fontWeight = FontWeight.SemiBold, color = uiOnSurface)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("General", "Home", "Icon", "Live").forEach { category ->
                SettingsOptionButton(
                    label = category,
                    selected = selectedCategory == category,
                    background = BrutalColors.Yellow,
                    modifier = Modifier.weight(1f)
                ) { selectedCategory = category }
            }
        }

        when (selectedCategory) {
            "General" -> {
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
                SettingsActionButton("RESET ALL CUSTOMIZATIONS", BrutalColors.Pink) { showResetConfirm = true }
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
            "Home" -> {
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
                    showPinnedAppPicker = true
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
            "Icon" -> {
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
                    showIconPackPicker = true
                }
            }
        }


            }
            "Live" -> {
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
                    SettingsActionButton(if (selectedChatApp == null) "CHOOSE APP" else "CHANGE APP", BrutalColors.Yellow, Modifier.weight(1f)) { showChatAppPicker = true }
                    if (selectedChatApp != null) SettingsActionButton("CLEAR", BrutalColors.White, Modifier.weight(.7f)) { onChatNotificationPackagesChange(emptyList()) }
                }
            }
        }


            }
        }
        Spacer(Modifier.height(12.dp))
    }

    if (showIconPackPicker) {
        AlertDialog(
            onDismissRequest = { showIconPackPicker = false },
            title = {
                Text(
                    text = "CHOOSE ICON PACK",
                    fontFamily = BrutalTypography.Display,
                    fontWeight = FontWeight.Normal
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = minOf(360.dp, (LocalConfiguration.current.screenHeightDp * 0.55f).dp)),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        val selected = iconPackPackage == null
                        BrutalBlock(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onIconPackChange(null)
                                    showIconPackPicker = false
                                },
                            background = if (selected) BrutalColors.Yellow else uiBackground,
                            borderWidth = 3.dp,
                            shadowX = if (selected) 0.dp else 3.dp,
                            shadowY = if (selected) 0.dp else 3.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SYSTEM ICONS",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) BrutalColors.Ink else uiOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (selected) "SELECTED" else "USE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) BrutalColors.Ink else uiOnSurface
                                )
                            }
                        }
                    }
                    items(
                        items = iconPacks,
                        key = { it.packageName }
                    ) { pack ->
                        val selected = iconPackPackage == pack.packageName
                        BrutalBlock(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onIconPackChange(pack.packageName)
                                    showIconPackPicker = false
                                },
                            background = if (selected) BrutalColors.Yellow else uiBackground,
                            borderWidth = 3.dp,
                            shadowX = if (selected) 0.dp else 3.dp,
                            shadowY = if (selected) 0.dp else 3.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pack.label.uppercase(Locale.ENGLISH),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) BrutalColors.Ink else uiOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (selected) "SELECTED" else "USE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) BrutalColors.Ink else uiOnSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showPinnedAppPicker) {
        AlertDialog(
            onDismissRequest = { showPinnedAppPicker = false },
            title = {
                Text(
                    text = "CHOOSE PINNED APPS",
                    fontFamily = BrutalTypography.Display,
                    fontWeight = FontWeight.Normal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = favorites.size.toString() + " / " + homeAppCount + " SELECTED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (pinnedCandidates.isEmpty()) {
                        Text(
                            text = "NO LAUNCHABLE APPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = minOf(360.dp, (LocalConfiguration.current.screenHeightDp * 0.55f).dp)),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(
                                items = pinnedCandidates,
                                key = { it.packageName + "/" + it.activityName }
                            ) { app ->
                                val key = app.packageName + "/" + app.activityName
                                val selected = favorites.contains(key)
                                val enabled = selected || favorites.size < homeAppCount
                                BrutalBlock(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            enabled = enabled,
                                            onClick = { onTogglePinnedApp(app) }
                                        ),
                                    background = if (selected) BrutalColors.Yellow else uiBackground,
                                    borderWidth = 3.dp,
                                    shadowX = if (selected) 0.dp else 3.dp,
                                    shadowY = if (selected) 0.dp else 3.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = app.label.uppercase(Locale.ENGLISH),
                                            modifier = Modifier.weight(1f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = uiOnSurface.copy(alpha = if (enabled) 1f else .45f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (selected) "SELECTED" else "ADD",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = uiOnSurface.copy(alpha = if (enabled) 1f else .45f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showChatAppPicker) {
        AlertDialog(onDismissRequest = { showChatAppPicker = false }, title = { Text("CHOOSE CHAT APP", fontFamily = BrutalTypography.Display, fontWeight = FontWeight.SemiBold) }, text = {
            LazyColumn(modifier = Modifier.heightIn(max = minOf(360.dp, (LocalConfiguration.current.screenHeightDp * 0.55f).dp)), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(chatCandidates, key = { it.packageName + "/" + it.activityName }) { app ->
                    BrutalBlock(Modifier.fillMaxWidth().clickable { onChatNotificationPackagesChange(listOf(app.packageName)); showChatAppPicker = false }, background = if(app.packageName == chatNotificationPackages.firstOrNull()) BrutalColors.Yellow else uiSurface, borderWidth = 3.dp, shadowX = 3.dp, shadowY = 3.dp) {
                        Text(app.label.uppercase(Locale.ENGLISH), fontSize = 12.sp, fontWeight = FontWeight.Black, color = if(app.packageName == chatNotificationPackages.firstOrNull()) BrutalColors.Ink else uiOnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }, confirmButton = {})
    }

    if (showResetConfirm) {
        AlertDialog(onDismissRequest = { showResetConfirm = false }, title = { Text("RESET ALL CUSTOMIZATIONS", fontFamily = BrutalTypography.Display) }, text = { Text("This clears launcher preferences, tile layout, pinned apps, notes/tasks, wallpaper and icon-pack selection. This cannot be undone.") }, dismissButton = { SettingsActionButton("CANCEL", BrutalColors.White) { showResetConfirm = false } }, confirmButton = { SettingsActionButton("RESET", BrutalColors.Pink) { showResetConfirm = false; onResetAll() } })
    }
}
