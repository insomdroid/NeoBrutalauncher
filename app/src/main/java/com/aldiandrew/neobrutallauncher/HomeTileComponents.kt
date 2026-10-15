package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
internal fun FixedSmallTile(
    modifier: Modifier,
    background: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    BrutalBlock(
        modifier = modifier.aspectRatio(1f),
        background = background,
        borderWidth = 3.dp,
        shadowX = 5.dp,
        shadowY = 5.dp,
        content = content
    )
}

@Composable
internal fun NeoQuoteTilePlain(
    quote: String,
    modifier: Modifier,
    paletteIndex: Int
) {
    val palette = NeoQuotes.paletteForRotation(paletteIndex)

    BrutalBlock(
        modifier = modifier,
        background = palette.background,
        borderWidth = 4.dp,
        borderColor = BrutalColors.Ink,
        shadowX = 6.dp,
        shadowY = 6.dp,
        shadowColor = BrutalColors.Ink
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 9.dp, top = 9.dp, end = 9.dp, bottom = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "“" + quote + "”",
                    fontFamily = BrutalTypography.Body,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = palette.text,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
    }
}

@Composable
fun NeoQuoteTile(
    quote: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    paletteIndex: Int = 0,
    showLabel: Boolean = false
) {
    val palette = NeoQuotes.paletteForRotation(paletteIndex)

    BrutalBlock(
        modifier = modifier,
        background = palette.background,
        borderWidth = 4.dp,
        borderColor = BrutalColors.Ink,
        shadowX = 6.dp,
        shadowY = 6.dp,
        shadowColor = BrutalColors.Ink
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 9.dp, top = 9.dp, end = 9.dp, bottom = 10.dp)
        ) {
                val compact = minOf(maxWidth, maxHeight)
                val (quoteSize, estimatedLines) = remember(quote, emphasized, maxWidth, maxHeight, showLabel) {
                    val availableHeight = (
                        maxHeight.value -
                            (if (emphasized) 20f else 30f) -
                            if (showLabel) 12f else 0f
                        ).coerceAtLeast(18f)
                    var resolvedSize = if (emphasized) {
                        val heightFactor = if (quote.length <= 55) 0.58f else 0.43f
                        minOf(34f, maxWidth.value * 0.11f, availableHeight * heightFactor)
                    } else {
                        when {
                            compact < 78.dp -> 7f
                            compact < 155.dp -> 12f
                            else -> 17f
                        }
                    }.coerceAtLeast(7f)

                    fun estimateLines(size: Float): Int {
                        val charsPerLine = (maxWidth.value / (size * 0.52f)).toInt().coerceAtLeast(8)
                        return ((quote.length + 2 + charsPerLine - 1) / charsPerLine)
                            .coerceAtLeast(1)
                    }

                    val maxQuoteLines = if (emphasized) 2 else 7
                    while (resolvedSize > 4.5f) {
                        val lines = estimateLines(resolvedSize)
                        val neededHeight = lines.coerceAtMost(maxQuoteLines) * resolvedSize * 1.08f
                        if (lines <= maxQuoteLines && neededHeight <= availableHeight) break
                        resolvedSize -= 0.5f
                    }
                    resolvedSize to estimateLines(resolvedSize).coerceAtMost(maxQuoteLines)
                }
                        

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(if (emphasized) 6.dp else 5.dp)
                ) {
                    if (showLabel) {
                        Text(
                            text = "QUOTE",
                            fontSize = 8.sp,
                            lineHeight = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = palette.text,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "“$quote”",
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = BrutalTypography.Body,
                        fontSize = quoteSize.sp,
                        lineHeight = (quoteSize * 1.08f).sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.text,
                        maxLines = estimatedLines,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
    }
}

internal fun appTileLayoutVariant(app: AppInfo, tileSize: NeoTileSize): Int {
    // Stable per app + size: no random layout changes across recomposition or restarts.
    val layoutKey = app.packageName + "/" + app.activityName + ":" + tileSize.name
    return Math.floorMod(layoutKey.hashCode(), 5)
}

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
