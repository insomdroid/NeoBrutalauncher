package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.util.Date

@Composable
internal fun HomeTopBar(
    now: java.util.Date,
    timeFormatter: java.text.SimpleDateFormat,
    longDayFormatter: java.text.SimpleDateFormat,
    longDateFormatter: java.text.SimpleDateFormat,
    homeClockDoubleTapEnabled: Boolean,
    homeClockLongPressEnabled: Boolean,
    onOpenSettings: () -> Unit,
    onOpenApps: () -> Unit
) {
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
                                text = timeFormatter.format(now),
                                fontFamily = BrutalTypography.Display,
                                fontSize = 23.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = BrutalColors.Ink
                            )
                            Text(
                                text = (longDayFormatter.format(now) + " / " + longDateFormatter.format(now)).uppercase(Locale.getDefault()),
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

@Composable
internal fun HomeAppTiles(
    context: android.content.Context,
    launchableApps: List<AppInfo>,
    tileSizes: Map<String, NeoTileSize>,
    palette: List<Color>,
    homeMusicBackground: Color,
    isDarkTheme: Boolean,
    appTileContentMode: TileContentMode,
    onLaunch: (AppInfo) -> Unit,
    onSelectTile: (NeoTileSpec) -> Unit,
    tileEditMode: Boolean,
    tilePositions: Map<String, NeoTilePosition>,
    appTileIds: Set<String>,
    onTilePositionsChange: (Map<String, NeoTilePosition>) -> Unit,
    onTileMoveFinished: () -> Unit
) {
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
                                            onSelectTile(NeoTileSpec(
                                                id = "home_music",
                                                size = tileSizes["home_music"] ?: NeoTileSize.FOUR_BY_ONE,
                                                label = "MUSIC"
                                            ) {})
                                        },
                                        interactionEnabled = !tileEditMode
                                    )
                                }
                            )
                        },
                        positions = tilePositions.filterKeys { appTileIds.contains(it) },
                        onPositionsChange = onTilePositionsChange,
                        onTileLongPress = { onSelectTile(it) },
                        onTileEdit = { onSelectTile(it) },
                        onTileMoveFinished = onTileMoveFinished,
                        editMode = tileEditMode,
                        modifier = Modifier.fillMaxWidth(),
                        gap = 10.dp
                )
}

@Composable
internal fun HomeSystemTiles(
    context: android.content.Context,
    showWeather: Boolean,
    weatherRefreshToken: Int,
    onRefreshWeather: () -> Unit
) {
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

@Composable
internal fun HomeQuoteImage(quote: String) {
                NeoTileGrid(
                    tiles = listOf(
                        NeoTileSpec(
                            id = "home_quote_image",
                            size = NeoTileSize.FOUR_BY_ONE,
                            label = "QUOTE"
                        ) {
                            NeoQuoteImageTile(
                                quote = quote,
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
