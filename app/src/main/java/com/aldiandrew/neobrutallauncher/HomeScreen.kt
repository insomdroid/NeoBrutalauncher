package com.aldiandrew.neobrutallauncher

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
internal fun HomeScreen(
    apps: List<AppInfo>,
    favorites: Set<String>,
    homeAppCount: Int,
    use24Hour: Boolean,
    showAmPm: Boolean,
    showWeather: Boolean,
    weatherRefreshToken: Int,
    onRefreshWeather: () -> Unit,
    appTileContentMode: TileContentMode,
    typographyStyle: TypographyStyle,
    wallpaperUri: String?,
    homeClockDoubleTapEnabled: Boolean,
    homeClockLongPressEnabled: Boolean,
    onOpenSettings: () -> Unit,
    onOpenApps: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onHomeAppCountChange: (Int) -> Unit,
    onRemovePinnedApp: (AppInfo) -> Unit,
    tilePositions: Map<String, NeoTilePosition>,
    onTilePositionsChange: (Map<String, NeoTilePosition>) -> Unit,
    tileSizes: Map<String, NeoTileSize>,
    onTileSizeChange: (String, NeoTileSize) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { LauncherPreferences(context) }
    val now = rememberMinuteClock()
    val homeClockBackground = BrutalColors.Yellow
    val homeClockText = BrutalColors.Ink
    val homeMusicBackground = if (MaterialTheme.colorScheme.background == BrutalColors.DarkPaper) BrutalColors.DarkTile else BrutalColors.Cyan
    val isDarkTheme = MaterialTheme.colorScheme.background == BrutalColors.DarkPaper
    val quoteRotation = rememberLiveTileData(
        tileId = "home-quotes",
        refreshIntervalMillis = 30L * 60L * 1000L,
        initialValue = (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    ) {
        (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    }.value ?: 0

    var taskItems by remember { mutableStateOf(preferences.taskItems()) }
    var selectedTile by remember { mutableStateOf<NeoTileSpec?>(null) }
    var tileEditMode by remember { mutableStateOf(false) }
    var excludedHomeApps by remember { mutableStateOf(preferences.excludedHomeApps()) }

    val timePattern = when {
        use24Hour -> "HH:mm"
        showAmPm -> "hh:mm a"
        else -> "hh:mm"
    }
    val time = remember(timePattern) { SimpleDateFormat(timePattern, Locale.getDefault()) }
    val longDay = remember { SimpleDateFormat("EEEE", Locale.ENGLISH) }
    val longDate = remember { SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH) }

    val appKeys = remember(apps) {
        apps.map { it.packageName + "/" + it.activityName }
    }
    val storedHomeOrder = remember(appKeys) { preferences.homeAppOrder() }
    var stableHomeOrder by remember(appKeys, storedHomeOrder) {
        mutableStateOf(
            run {
                val available = appKeys.toSet()
                val existing = storedHomeOrder.filter { it in available }
                val missing = appKeys.filterNot { it in existing }
                if (existing.isNotEmpty()) {
                    existing + missing
                } else {
                    appKeys
                }
            }
        )
    }

    LaunchedEffect(stableHomeOrder) {
        preferences.setHomeAppOrder(stableHomeOrder)
    }

    // A pinned app is a Home launchable app, so pinning also clears a prior removal.
    LaunchedEffect(favorites, excludedHomeApps) {
        val cleaned = excludedHomeApps - favorites
        if (cleaned != excludedHomeApps) {
            excludedHomeApps = cleaned
            preferences.setExcludedHomeApps(cleaned)
        }
    }

    val appsByKey = remember(apps) {
        apps.associateBy { it.packageName + "/" + it.activityName }
    }
    val launchableApps = remember(
        stableHomeOrder,
        excludedHomeApps,
        favorites,
        appsByKey,
        homeAppCount,
        tilePositions
    ) {
        stableHomeOrder
            .filterNot { excludedHomeApps.contains(it) }
            .sortedWith(
                compareByDescending<String> { favorites.contains(it) }
            )
            .mapNotNull { appsByKey[it] }
            .take(homeAppCount.coerceIn(2, 8))
            .sortedWith(
                compareBy<AppInfo> {
                    tilePositions["app_" + it.packageName + "_" + it.activityName]?.row
                        ?: Int.MAX_VALUE
                }
            )
    }

    val palette = remember(BrutalColors.activePreset) { BrutalColors.appPalette(0) }
    val appTileIds = remember(launchableApps) {
        launchableApps.map { "app_" + it.packageName + "_" + it.activityName }.toSet() +
            setOf("home_music", "home_quote_image")
    }
    val homeQuotePair = remember(quoteRotation) {
        NeoQuotes.pairForRotation(quoteRotation)
    }
    val homeQuote = homeQuotePair.first
    val homeQuoteHeight = when {
        homeQuote.length > 135 -> 112.dp
        homeQuote.length > 90 -> 96.dp
        homeQuote.length > 50 -> 84.dp
        else -> 76.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (wallpaperUri == null) MaterialTheme.colorScheme.background else Color.Transparent)
    ) {
        BrutalWallpaper(uriString = wallpaperUri, modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "top-bar") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrutalBlock(
                        modifier = Modifier.weight(1f).padding(end = 8.dp).combinedClickable(
                            onClick = {},
                            onDoubleClick = if (homeClockDoubleTapEnabled) onOpenSettings else null,
                            onLongClick = if (homeClockLongPressEnabled) onOpenApps else null
                        ),
                        background = BrutalColors.Cyan,
                        borderWidth = 3.dp,
                        shadowX = 4.dp,
                        shadowY = 4.dp
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = time.format(now),
                                fontFamily = BrutalTypography.Display,
                                fontSize = 23.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = BrutalColors.Ink
                            )
                            Text(
                                text = (longDay.format(now) + " / " + longDate.format(now)).uppercase(Locale.getDefault()),
                                fontSize = 8.sp,
                                lineHeight = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.4.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = BrutalColors.Ink
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(106.dp)
                            .clickable(onClick = onOpenSettings)
                    ) {
                        BrutalBlock(
                            modifier = Modifier.fillMaxWidth(),
                            background = BrutalColors.Pink,
                            borderWidth = 3.dp,
                            shadowX = 4.dp,
                            shadowY = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = BrutalColors.Ink,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "SETTINGS",
                                    fontFamily = BrutalTypography.Display,
                                    modifier = Modifier,
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = BrutalColors.Ink
                                )
                            }
                        }
                    }
                }
            }

            item(key = "home-quote") {
                NeoQuoteTile(
                    quote = homeQuote,
                    modifier = Modifier.fillMaxWidth().height(homeQuoteHeight),
                    emphasized = true,
                    paletteIndex = quoteRotation
                )
            }

            item(key = "launchable-apps") {
                NeoTileGrid(
                        tiles = buildList {
                            launchableApps.forEachIndexed { index, app ->
                                val id = "app_" + app.packageName + "_" + app.activityName
                                val defaultSize = when {
                                    index < 2 -> NeoTileSize.HORIZONTAL
                                    else -> NeoTileSize.SMALL
                                }
                                val size = tileSizes[id] ?: defaultSize
                                val stableVariant = appTileLayoutVariant(app, size)
                                val tileColor = palette[Math.floorMod(app.packageName.hashCode(), palette.size)]

                                add(
                                    NeoTileSpec(
                                        id = id,
                                        size = size,
                                        label = app.label.uppercase(),
                                        onClick = { onLaunch(app) }
                                    ) {
                                        AppTile(
                                            app = app,
                                            background = tileColor,
                                            modifier = Modifier.fillMaxSize(),
                                            contentMode = appTileContentMode,
                                            tileSize = size,
                                            variant = stableVariant
                                        )
                                    }
                                )
                            }
                            add(
                                NeoTileSpec(
                                    id = "home_music",
                                    size = tileSizes["home_music"] ?: NeoTileSize.FOUR_BY_ONE,
                                    label = "MUSIC"
                                ) {
                                    NeoMusicTile(
                                        context = context,
                                        modifier = Modifier.fillMaxSize(),
                                        background = homeMusicBackground,
                                        textColor = if (isDarkTheme) BrutalColors.DarkWhite else BrutalColors.Ink,
                                        onLongClick = {
                                            selectedTile = NeoTileSpec(
                                                id = "home_music",
                                                size = tileSizes["home_music"] ?: NeoTileSize.FOUR_BY_ONE,
                                                label = "MUSIC"
                                            ) {}
                                        },
                                        interactionEnabled = !tileEditMode
                                    )
                                }
                            )
                        },
                        positions = tilePositions.filterKeys { appTileIds.contains(it) },
                        onPositionsChange = onTilePositionsChange,
                        onTileLongPress = { selectedTile = it },
                        onTileEdit = { selectedTile = it },
                        onTileMoveFinished = { tileEditMode = false },
                        editMode = tileEditMode,
                        modifier = Modifier.fillMaxWidth(),
                        gap = 10.dp
                )
            }

            item(key = "system-tiles") {
                NeoTileGrid(
                    tiles = buildList {
                        add(
                            NeoTileSpec(
                                id = "system_battery",
                                size = NeoTileSize.SMALL,
                                label = "BATTERY"
                            ) {
                                BatteryTile(
                                    context = context,
                                    modifier = Modifier.fillMaxSize(),
                                    background = BrutalColors.Yellow
                                )
                            }
                        )
                        add(
                            NeoTileSpec(
                                id = "system_network",
                                size = NeoTileSize.SMALL,
                                label = "NETWORK"
                            ) {
                                NeoNetworkTile(
                                    context = context,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        )
                        if (showWeather) {
                            add(
                                NeoTileSpec(
                                    id = "system_weather",
                                    size = NeoTileSize.SMALL,
                                    label = "WEATHER",
                                    onClick = onRefreshWeather
                                ) {
                                    NeoWeatherTile(
                                        context = context,
                                        refreshToken = weatherRefreshToken,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            )
                        }
                    },
                    positions = emptyMap(),
                    onPositionsChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    gap = 10.dp
                )
            }

            item(key = "home-quote-image") {
                NeoTileGrid(
                    tiles = listOf(
                        NeoTileSpec(
                            id = "home_quote_image",
                            size = NeoTileSize.FOUR_BY_ONE,
                            label = "QUOTE"
                        ) {
                            NeoQuoteImageTile(
                                quote = homeQuotePair.second,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    ),
                    positions = emptyMap(),
                    onPositionsChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    gap = 10.dp
                )
            }

        }
    }

    selectedTile?.let { tile ->
        val tileApp = launchableApps.firstOrNull {
            ("app_" + it.packageName + "_" + it.activityName) == tile.id
        }
        val isPinnedApp = tileApp?.let {
            favorites.contains(it.packageName + "/" + it.activityName)
        } == true

        AlertDialog(
            onDismissRequest = {
                selectedTile = null
                tileEditMode = false
            },
            title = {
                Text(
                    text = "EDIT TILE",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CURRENT SIZE: " + tile.size.label,
                        fontWeight = FontWeight.Black
                    )

                    listOf(
                        NeoTileSize.SMALL,
                        NeoTileSize.HORIZONTAL,
                        NeoTileSize.THREE_BY_ONE,
                        NeoTileSize.FOUR_BY_ONE
                    ).forEach { option ->
                        BrutalActionButton(
                            title = "SET ${option.label}",
                            background = if (tile.size == option) BrutalColors.Yellow else BrutalColors.White
                        ) {
                            onTileSizeChange(tile.id, option)
                            selectedTile = null
                            tileEditMode = false
                        }
                    }
                    BrutalActionButton(
                        title = "MOVE TILE",
                        background = BrutalColors.Cyan
                    ) {
                        selectedTile = null
                        tileEditMode = true
                    }

                    if (isPinnedApp && tileApp != null) {
                        BrutalActionButton(
                            title = "REMOVE PIN",
                            background = BrutalColors.Pink
                        ) {
                            onRemovePinnedApp(tileApp)
                            selectedTile = null
                            tileEditMode = false
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        selectedTile = null
                        tileEditMode = false
                    }
                ) {
                    Text("DONE", fontWeight = FontWeight.Black)
                }
            }
        )
    }
    }
