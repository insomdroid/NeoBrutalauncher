package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

@Composable
internal fun AppTile(
    app: AppInfo,
    background: Color,
    modifier: Modifier,
    contentMode: TileContentMode,
    tileSize: NeoTileSize,
    variant: Int
) {
    BoxWithConstraints(modifier = modifier) {
        // Capture the tile constraints before entering nested Box/Row content scopes.
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        val iconBitmap = remember(app.packageName, app.icon) {
            app.icon.toBitmap(64, 64).asImageBitmap()
        }
        val compactTile = tileSize == NeoTileSize.SMALL
        val iconSize = when (tileSize) {
            NeoTileSize.SMALL -> minOf(availableWidth * 0.34f, availableHeight * 0.36f, 26.dp)
            NeoTileSize.HORIZONTAL -> minOf(availableHeight * 0.38f, 30.dp)
            NeoTileSize.THREE_BY_ONE -> minOf(availableHeight * 0.42f, 34.dp)
            NeoTileSize.FOUR_BY_ONE, NeoTileSize.FOUR_BY_TWO -> minOf(availableHeight * 0.72f, 72.dp)
        }
        val maxTextSize = when (tileSize) {
            NeoTileSize.SMALL -> 9.sp
            NeoTileSize.HORIZONTAL -> 15.sp
            NeoTileSize.THREE_BY_ONE -> if (availableWidth < 220.dp) 18.sp else 23.sp
            NeoTileSize.FOUR_BY_ONE, NeoTileSize.FOUR_BY_TWO -> if (availableWidth < 300.dp) 23.sp else 30.sp
        }
        val textMaxLines = 2

        BrutalBlock(
            modifier = Modifier.fillMaxSize(),
            background = background,
            borderWidth = 3.dp,
            shadowX = 5.dp,
            shadowY = 5.dp
        ) {
            Box(Modifier.fillMaxSize()) {
                when (contentMode) {
                    TileContentMode.ICON -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Image(
                                bitmap = iconBitmap,
                                contentDescription = app.label,
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    }

                    TileContentMode.TEXT -> {
                        val isFourByOne = tileSize == NeoTileSize.FOUR_BY_ONE || tileSize == NeoTileSize.FOUR_BY_TWO
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = if (isFourByOne) Alignment.CenterEnd else if (compactTile) Alignment.Center else Alignment.CenterStart
                        ) {
                            Text(
                                text = app.label.uppercase(),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = if (isFourByOne) TextAlign.End else if (compactTile) TextAlign.Center else TextAlign.Start,
                                fontFamily = BrutalTypography.Display,
                                fontSize = maxTextSize,
                                lineHeight = (maxTextSize.value * 1.02f).sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink,
                                maxLines = textMaxLines,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    TileContentMode.ICON_TEXT -> {
                        val appTitle: @Composable (Modifier, TextAlign, Int) -> Unit =
                            { textModifier, alignment, lines ->
                                Text(
                                    text = app.label.uppercase(),
                                    modifier = textModifier,
                                    textAlign = alignment,
                                    fontFamily = BrutalTypography.Display,
                                    fontSize = maxTextSize,
                                    lineHeight = (maxTextSize.value * 1.02f).sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrutalColors.Ink,
                                    maxLines = lines,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        val appIcon: @Composable (Modifier) -> Unit = { iconModifier ->
                            Image(
                                bitmap = iconBitmap,
                                contentDescription = app.label,
                                modifier = iconModifier.size(iconSize)
                            )
                        }
                        val layout = Math.floorMod(variant, 5)

                        when (tileSize) {
                            NeoTileSize.SMALL -> {
                                // Match the compact Wi-Fi tile: centered icon, readable two-line label.
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(5.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    appIcon(Modifier)
                                    appTitle(
                                        Modifier.fillMaxWidth(),
                                        TextAlign.Center,
                                        2
                                    )
                                }
                            }

                            NeoTileSize.HORIZONTAL -> {
                                if (availableHeight < 66.dp || availableWidth < 130.dp) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        appIcon(Modifier)
                                        appTitle(Modifier.weight(1f), TextAlign.Start, 1)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        when (layout) {
                                            0 -> Row(
                                                Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                appIcon(Modifier)
                                                appTitle(Modifier.weight(1f), TextAlign.Start, 2)
                                            }
                                            1 -> Row(
                                                Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                appTitle(Modifier.weight(1f), TextAlign.Start, 2)
                                                appIcon(Modifier)
                                            }
                                            2 -> {
                                                appIcon(Modifier.align(Alignment.TopStart))
                                                appTitle(
                                                    Modifier.align(Alignment.BottomEnd).fillMaxWidth(0.78f),
                                                    TextAlign.End,
                                                    1
                                                )
                                            }
                                            3 -> {
                                                appTitle(
                                                    Modifier.align(Alignment.TopStart).fillMaxWidth(0.78f),
                                                    TextAlign.Start,
                                                    1
                                                )
                                                appIcon(Modifier.align(Alignment.BottomEnd))
                                            }
                                            else -> Row(
                                                Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                appIcon(Modifier)
                                                appTitle(Modifier.weight(1f), TextAlign.Center, 2)
                                            }
                                        }
                                    }
                                }
                            }

                            NeoTileSize.THREE_BY_ONE,
                            NeoTileSize.FOUR_BY_ONE, NeoTileSize.FOUR_BY_TWO -> {
                                // Width/height checks keep the selected composition from colliding.
                                if (availableHeight < 66.dp || availableWidth < 190.dp) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        appIcon(Modifier)
                                        appTitle(Modifier.weight(1f), TextAlign.Start, 2)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(
                                                horizontal = if (tileSize == NeoTileSize.FOUR_BY_ONE || tileSize == NeoTileSize.FOUR_BY_TWO) 14.dp else 9.dp,
                                                vertical = 7.dp
                                            )
                                    ) {
                                        when (layout) {
                                            0 -> Row(
                                                Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                appIcon(Modifier)
                                                appTitle(Modifier.weight(1f), TextAlign.Start, 2)
                                            }
                                            1 -> {
                                                appTitle(
                                                    Modifier.align(Alignment.CenterStart)
                                                        .fillMaxWidth(0.76f)
                                                        .padding(end = iconSize + 8.dp),
                                                    TextAlign.Start,
                                                    2
                                                )
                                                appIcon(Modifier.align(Alignment.CenterEnd))
                                            }
                                            2 -> {
                                                appIcon(Modifier.align(Alignment.TopEnd))
                                                appTitle(
                                                    Modifier.align(Alignment.BottomStart)
                                                        .fillMaxWidth(0.82f)
                                                        .padding(end = iconSize + 8.dp),
                                                    TextAlign.Start,
                                                    2
                                                )
                                            }
                                            3 -> {
                                                appTitle(
                                                    Modifier.align(Alignment.TopStart)
                                                        .fillMaxWidth(0.82f)
                                                        .padding(end = iconSize + 8.dp),
                                                    TextAlign.Start,
                                                    2
                                                )
                                                appIcon(Modifier.align(Alignment.BottomEnd))
                                            }
                                            else -> {
                                                appIcon(Modifier.align(Alignment.BottomStart))
                                                appTitle(
                                                    Modifier.align(Alignment.TopEnd)
                                                        .fillMaxWidth(0.78f)
                                                        .padding(start = iconSize + 8.dp),
                                                    TextAlign.End,
                                                    2
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
