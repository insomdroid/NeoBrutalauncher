package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp






























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

internal fun appTileLayoutVariant(app: AppInfo, tileSize: NeoTileSize): Int =
    HomeTileLayoutRules.variant(app.packageName, app.activityName, tileSize.name)
