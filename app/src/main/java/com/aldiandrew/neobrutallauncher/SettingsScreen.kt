package com.aldiandrew.neobrutallauncher

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

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
                SettingsGeneralSection(
                    context = context,
                    uiSurface = uiSurface,
                    uiOnSurface = uiOnSurface,
                    isDark = isDark,
                    themePreference = themePreference,
                    onThemeChange = onThemeChange,
                    designPreset = designPreset,
                    onDesignPresetChange = onDesignPresetChange,
                    typographyStyle = typographyStyle,
                    onTypographyStyleChange = onTypographyStyleChange,
                    wallpaperUri = wallpaperUri,
                    onChooseWallpaper = onChooseWallpaper,
                    onClearWallpaper = onClearWallpaper,
                    use24Hour = use24Hour,
                    showAmPm = showAmPm,
                    onUse24HourChange = onUse24HourChange,
                    onShowAmPmChange = onShowAmPmChange,
                    motionSmoothness = motionSmoothness,
                    onMotionSmoothnessChange = onMotionSmoothnessChange,
                    reduceMotion = reduceMotion,
                    onReduceMotionChange = onReduceMotionChange,
                    onBackup = onBackup,
                    onRestore = onRestore,
                    onOpenResetConfirm = { showResetConfirm = true }
                )
            }
            "Home" -> {
                SettingsHomeSection(
                    uiSurface = uiSurface,
                    uiOnSurface = uiOnSurface,
                    isDark = isDark,
                    appTileContentMode = appTileContentMode,
                    onAppTileContentModeChange = onAppTileContentModeChange,
                    favoritesCount = favoritesCount,
                    homeAppCount = homeAppCount,
                    onHomeAppCountChange = onHomeAppCountChange,
                    onOpenPinnedAppPicker = { showPinnedAppPicker = true },
                    onClearFavorites = onClearFavorites,
                    swipeDownNotifications = swipeDownNotifications,
                    onSwipeDownNotificationsChange = onSwipeDownNotificationsChange,
                    homeClockDoubleTapEnabled = homeClockDoubleTapEnabled,
                    onHomeClockDoubleTapChange = onHomeClockDoubleTapChange,
                    homeClockLongPressEnabled = homeClockLongPressEnabled,
                    onHomeClockLongPressChange = onHomeClockLongPressChange
                )
            }
            "Icon" -> {
                SettingsIconSection(
                    uiSurface = uiSurface,
                    uiOnSurface = uiOnSurface,
                    isDark = isDark,
                    selectedIconPack = selectedIconPack,
                    onOpenIconPackPicker = { showIconPackPicker = true }
                )
            }
            "Live" -> {
                SettingsLiveSection(
                    uiBackground = uiBackground,
                    uiSurface = uiSurface,
                    uiOnSurface = uiOnSurface,
                    isDark = isDark,
                    showWeather = showWeather,
                    onShowWeatherChange = onShowWeatherChange,
                    locationPermissionGranted = locationPermissionGranted,
                    onRequestWeatherPermission = onRequestWeatherPermission,
                    notificationAccessGranted = notificationAccessGranted,
                    onOpenNotificationAccess = onOpenNotificationAccess,
                    selectedChatApp = selectedChatApp,
                    onOpenChatAppPicker = { showChatAppPicker = true },
                    onChatNotificationPackagesChange = onChatNotificationPackagesChange
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }

    SettingsDialogs(
        showIconPackPicker = showIconPackPicker,
        onShowIconPackPickerChange = { showIconPackPicker = it },
        showPinnedAppPicker = showPinnedAppPicker,
        onShowPinnedAppPickerChange = { showPinnedAppPicker = it },
        showChatAppPicker = showChatAppPicker,
        onShowChatAppPickerChange = { showChatAppPicker = it },
        showResetConfirm = showResetConfirm,
        onShowResetConfirmChange = { showResetConfirm = it },
        iconPacks = iconPacks,
        iconPackPackage = iconPackPackage,
        onIconPackChange = onIconPackChange,
        uiBackground = uiBackground,
        uiSurface = uiSurface,
        uiOnSurface = uiOnSurface,
        pinnedCandidates = pinnedCandidates,
        favorites = favorites,
        homeAppCount = homeAppCount,
        onTogglePinnedApp = onTogglePinnedApp,
        chatCandidates = chatCandidates,
        chatNotificationPackages = chatNotificationPackages,
        onChatNotificationPackagesChange = onChatNotificationPackagesChange,
        onResetAll = onResetAll
    )
}
