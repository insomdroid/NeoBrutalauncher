package com.aldiandrew.neobrutallauncher

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NeoBrutalLauncherApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { AppRepository(context) }
    val preferences = remember { LauncherPreferences(context) }

    var apps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var currentPage by remember { mutableStateOf(0) }
    var settingsOpen by remember { mutableStateOf(false) }
    var onboardingCompleted by remember { mutableStateOf(preferences.onboardingCompleted()) }

    var themePreference by remember { mutableStateOf(preferences.theme()) }
    var designPreset by remember { mutableStateOf(preferences.designPreset()) }
    var use24Hour by remember { mutableStateOf(preferences.use24Hour()) }
    var showAmPm by remember { mutableStateOf(preferences.showAmPm()) }
    var homeAppCount by remember { mutableStateOf(preferences.homeAppCount()) }
    var showWeather by remember { mutableStateOf(preferences.showWeather()) }
    var weatherRefreshToken by remember { mutableIntStateOf(0) }
    var favorites by remember { mutableStateOf(preferences.favorites()) }
    var tilePositions by remember { mutableStateOf(preferences.tilePositions()) }
    var tileSizes by remember { mutableStateOf(preferences.tileSizes()) }
    var appTileContentMode by remember { mutableStateOf(preferences.appTileContentMode()) }
    var typographyStyle by remember { mutableStateOf(preferences.typographyStyle()) }
    var iconPackPackage by remember { mutableStateOf(preferences.iconPackPackage()) }
    var wallpaperUri by remember { mutableStateOf(preferences.wallpaperUri()) }
    var motionSmoothness by remember { mutableStateOf(preferences.motionSmoothness()) }
    var reduceMotion by remember { mutableStateOf(preferences.reduceMotion()) }
    var settingsSwipeEnabled by remember { mutableStateOf(preferences.settingsSwipeEnabled()) }
    var swipeDownNotifications by remember { mutableStateOf(preferences.swipeDownNotifications()) }
    var homeClockDoubleTapEnabled by remember { mutableStateOf(preferences.homeClockDoubleTapEnabled()) }
    var homeClockLongPressEnabled by remember { mutableStateOf(preferences.homeClockLongPressEnabled()) }
    var locationPermissionGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var notificationAccessGranted by remember {
        mutableStateOf(
            NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        )
    }

    var selectedChatPackages by remember {
        mutableStateOf(preferences.chatNotificationPackages())
    }

    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            wallpaperUri = uri.toString()
            preferences.setWallpaperUri(uri.toString())
        }
    }

    val backupFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(preferences.exportBackupJson().toByteArray(Charsets.UTF_8))
                } ?: error("Unable to open backup destination")
            }.onFailure {
                android.widget.Toast.makeText(context, "BACKUP FAILED", android.widget.Toast.LENGTH_SHORT).show()
            }.onSuccess {
                android.widget.Toast.makeText(context, "BACKUP SAVED", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val result = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > 512 * 1024) error("Backup too large")
                        output.write(buffer, 0, read)
                    }
                    preferences.importBackupJson(output.toString(Charsets.UTF_8.name()))
                } ?: false
            }.getOrDefault(false)

            if (result) {
                android.widget.Toast.makeText(context, "BACKUP RESTORED", android.widget.Toast.LENGTH_SHORT).show()
                (context as? Activity)?.recreate()
            } else {
                android.widget.Toast.makeText(context, "INVALID BACKUP", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationPermissionGranted = granted
        if (granted) {
            showWeather = true
            preferences.setShowWeather(true)
            weatherRefreshToken++
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val refreshScope = rememberCoroutineScope()

    fun refreshApps() {
        refreshScope.launch(Dispatchers.Default) {
            val loadedApps = repository.loadApps()
            withContext(Dispatchers.Main.immediate) {
                // AppRepository returns the same list instance while its cache is valid.
                // Avoid a structural list equality check on cache-hit refreshes.
                if (apps !== loadedApps) {
                    apps = loadedApps
                }
                if (preferences.onboardingCompleted() &&
                    !preferences.homeAppsInitialized() &&
                    favorites.isEmpty()
                ) {
                    val initialFavorites = loadedApps
                        .filter { it.packageName != context.packageName }
                        .take(5)
                        .map { it.packageName + "/" + it.activityName }
                        .toSet()
                    favorites = initialFavorites
                    preferences.setFavorites(initialFavorites)
                    preferences.setHomeAppsInitialized(true)
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locationPermissionGranted =
                    context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
                notificationAccessGranted =
                    NotificationManagerCompat.getEnabledListenerPackages(context)
                        .contains(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val motionConfig = NeoMotionConfig(
        smoothness = motionSmoothness,
        reduceMotion = reduceMotion
    )

    fun requestLaunch(app: AppInfo) {
        repository.launch(app)
    }

    LaunchedEffect(Unit) {
        refreshApps()
    }

    // Centralize system Back handling so pager pages cannot register competing callbacks.
    BackHandler(enabled = onboardingCompleted) {
        when {
            settingsOpen -> settingsOpen = false
            currentPage != 0 -> currentPage = 0
            else -> Unit
        }
    }

    when {
        !onboardingCompleted -> {
            NeoBrutalTheme(
                themePreference = themePreference,
                designPreset = designPreset,
                typographyStyle = typographyStyle,
            ) {
                NeoOnboardingScreen(
                    apps = apps,
                    initialFavorites = favorites,
                    favoriteLimit = homeAppCount,
                    locationPermissionGranted = locationPermissionGranted,
                    notificationAccessGranted = notificationAccessGranted,
                    onRequestLocationPermission = {
                        locationPermissionLauncher.launch(
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    },
                    onOpenNotificationAccess = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    onFavoritesChange = { updated ->
                        favorites = updated
                        preferences.setFavorites(updated)
                    },
                    onFinish = {
                        var selected = preferences.favorites()
                        if (selected.isEmpty()) {
                            selected = apps
                                .filter { it.packageName != context.packageName }
                                .take(homeAppCount)
                                .map { it.packageName + "/" + it.activityName }
                                .toSet()
                            favorites = selected
                            preferences.setFavorites(selected)
                        }
                        preferences.setHomeAppsInitialized(true)
                        preferences.setOnboardingCompleted(true)
                        onboardingCompleted = true
                    }
                )
            }
        }

        settingsOpen -> {
            NeoBrutalTheme(
                themePreference = themePreference,
                designPreset = designPreset,
                typographyStyle = typographyStyle,
            ) {
                CompositionLocalProvider(LocalNeoMotionConfig provides motionConfig) {
                    SettingsScreen(
                    themePreference = themePreference,
                    designPreset = designPreset,
                    use24Hour = use24Hour,
                    showAmPm = showAmPm,
                    homeAppCount = homeAppCount,
                    showWeather = showWeather,
                    appTileContentMode = appTileContentMode,
                    typographyStyle = typographyStyle,
                    iconPackPackage = iconPackPackage,
                    motionSmoothness = motionSmoothness,
                    reduceMotion = reduceMotion,
                    settingsSwipeEnabled = settingsSwipeEnabled,
                    swipeDownNotifications = swipeDownNotifications,
                    homeClockDoubleTapEnabled = homeClockDoubleTapEnabled,
                    homeClockLongPressEnabled = homeClockLongPressEnabled,
                    wallpaperUri = wallpaperUri,
                    favorites = favorites,
                    favoritesCount = favorites.size,
                    locationPermissionGranted = locationPermissionGranted,
                    notificationAccessGranted = notificationAccessGranted,
                    chatNotificationPackages = selectedChatPackages,
                    apps = apps,
                    onBack = { settingsOpen = false },
                    onThemeChange = {
                        themePreference = it
                        preferences.setTheme(it)
                    },
                    onDesignPresetChange = {
                        designPreset = it
                        preferences.setDesignPreset(it)
                    },
                    onUse24HourChange = {
                        use24Hour = it
                        preferences.setUse24Hour(it)
                    },
                    onShowAmPmChange = {
                        showAmPm = it
                        preferences.setShowAmPm(it)
                    },
                    onHomeAppCountChange = { count ->
                        val normalized = when (count) {
                            3, 5 -> count
                            else -> 5
                        }
                        homeAppCount = normalized
                        preferences.setHomeAppCount(normalized)

                        if (favorites.size > normalized) {
                            val favoriteKeysInAppOrder = apps.map {
                                it.packageName + "/" + it.activityName
                            }
                            val trimmed = favoriteKeysInAppOrder
                                .filter { favorites.contains(it) }
                                .take(normalized)
                                .toSet()
                            favorites = trimmed
                            preferences.setFavorites(trimmed)
                        }
                    },
                    onShowWeatherChange = { enabled ->
                        if (enabled) {
                            if (locationPermissionGranted) {
                                showWeather = true
                                preferences.setShowWeather(true)
                                weatherRefreshToken++
                            } else {
                                locationPermissionLauncher.launch(
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            }
                        } else {
                            showWeather = false
                            preferences.setShowWeather(false)
                        }
                    },
                    onRequestWeatherPermission = {
                        locationPermissionLauncher.launch(
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    },
                    onOpenNotificationAccess = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    onChatNotificationPackagesChange = { updated ->
                        val normalized = updated.distinct().take(1)
                        selectedChatPackages = normalized
                        preferences.setChatNotificationPackages(normalized)
                        NeoChatNotificationStore.setSelectedPackages(normalized)
                        NeoNotificationServiceRegistry.service?.refreshChatNotifications()
                    },
                    onAppTileContentModeChange = {
                        appTileContentMode = it
                        preferences.setAppTileContentMode(it)
                    },
                    onTypographyStyleChange = {
                        typographyStyle = it
                        preferences.setTypographyStyle(it)
                    },
                    onIconPackChange = {
                        iconPackPackage = it
                        preferences.setIconPackPackage(it)
                        refreshApps()
                    },
                    onTogglePinnedApp = { app ->
                        val key = app.packageName + "/" + app.activityName
                        val updated = favorites.toMutableSet()
                        if (updated.contains(key)) {
                            updated.remove(key)
                        } else if (updated.size < homeAppCount) {
                            updated.add(key)
                        }
                        favorites = updated
                        preferences.setFavorites(updated)
                    },
                    onMotionSmoothnessChange = {
                        motionSmoothness = it
                        preferences.setMotionSmoothness(it)
                    },
                    onReduceMotionChange = {
                        reduceMotion = it
                        preferences.setReduceMotion(it)
                    },
                    onSettingsSwipeChange = {
                        settingsSwipeEnabled = it
                        preferences.setSettingsSwipeEnabled(it)
                    },
                    onSwipeDownNotificationsChange = {
                        swipeDownNotifications = it
                        preferences.setSwipeDownNotifications(it)
                    },
                    onHomeClockDoubleTapChange = {
                        homeClockDoubleTapEnabled = it
                        preferences.setHomeClockDoubleTapEnabled(it)
                    },
                    onHomeClockLongPressChange = {
                        homeClockLongPressEnabled = it
                        preferences.setHomeClockLongPressEnabled(it)
                    },
                    onBackup = { backupFileLauncher.launch("neo-brutal-launcher-backup.json") },
                    onRestore = { restoreFileLauncher.launch(arrayOf("application/json", "text/json", "text/plain")) },
                    onResetAll = {
                        preferences.resetCustomizations()
                        (context as? Activity)?.recreate()
                    },
                    onChooseWallpaper = {
                        wallpaperPickerLauncher.launch(arrayOf("image/*"))
                    },
                    onClearWallpaper = {
                        wallpaperUri = null
                        preferences.setWallpaperUri(null)
                    },
                    onClearFavorites = {
                        favorites = emptySet()
                        preferences.clearFavorites()
                    }
                    )
                }
            }
        }

        else -> {
            NeoBrutalTheme(
                themePreference = themePreference,
                designPreset = designPreset,
                typographyStyle = typographyStyle,
            ) {
                CompositionLocalProvider(LocalNeoMotionConfig provides motionConfig) {
                    LauncherPageHost(
                        currentPage = currentPage,
                        motionConfig = motionConfig,
                        wallpaperUri = wallpaperUri,
                        swipeDownNotifications = swipeDownNotifications,
                    onPageChange = {
                        currentPage = it.coerceIn(0, 2)
                        if (currentPage == 1) refreshApps()
                    }
                ) { page ->
                    if (page == 0) {
                        HomeScreen(
                            apps = apps,
                            favorites = favorites,
                            homeAppCount = homeAppCount,
                            use24Hour = use24Hour,
                            showAmPm = showAmPm,
                            showWeather = showWeather,
                            weatherRefreshToken = weatherRefreshToken,
                            onRefreshWeather = { weatherRefreshToken++ },
                            appTileContentMode = appTileContentMode,
                            typographyStyle = typographyStyle,
                            wallpaperUri = wallpaperUri,
                            homeClockDoubleTapEnabled = homeClockDoubleTapEnabled,
                            homeClockLongPressEnabled = homeClockLongPressEnabled,
                            onOpenSettings = { settingsOpen = true },
                            onOpenApps = { currentPage = 1 },
                            onLaunch = ::requestLaunch,
                            onHomeAppCountChange = { updated ->
                                homeAppCount = updated
                                preferences.setHomeAppCount(updated)
                            },
                            onRemovePinnedApp = { app ->
                                val key = app.packageName + "/" + app.activityName
                                val updated = favorites - key
                                favorites = updated
                                preferences.setFavorites(updated)
                            },
                            tilePositions = tilePositions,
                            onTilePositionsChange = { updated ->
                                tilePositions = updated
                                preferences.setTilePositions(updated)
                            },
                            tileSizes = tileSizes,
                            onTileSizeChange = { tileId, size ->
                                tileSizes = tileSizes + (tileId to size)
                                preferences.setTileSizes(tileSizes)
                            },
                        )
                    } else if (page == 1) {
                        AppsPage(
                            apps = apps,
                            favorites = favorites,
                            onLaunch = ::requestLaunch,
                        )
                    } else {
                        LivePage(
                            apps = apps,
                            selectedChatPackages = selectedChatPackages,
                            showWeather = showWeather,
                            weatherRefreshToken = weatherRefreshToken,
                            onRefreshWeather = { weatherRefreshToken++ },
                            use24Hour = use24Hour,
                            showAmPm = showAmPm,
                            onSelectChatPackage = { packageName ->
                                val normalized = listOfNotNull(packageName).take(1)
                                selectedChatPackages = normalized
                                preferences.setChatNotificationPackages(normalized)
                                NeoChatNotificationStore.setSelectedPackages(normalized)
                                NeoNotificationServiceRegistry.service?.refreshChatNotifications()
                            },
                        )
                    }
                    }
                }
            }
        }
    }
}
