package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
internal fun LauncherPageHost(
    currentPage: Int,
    motionConfig: NeoMotionConfig,
    wallpaperUri: String?,
    swipeDownNotifications: Boolean,
    onPageChange: (Int) -> Unit,
    content: @Composable (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { 3 }
    )

    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage) {
            pagerState.animateScrollToPage(
                page = currentPage,
                animationSpec = if (motionConfig.reduceMotion) {
                    androidx.compose.animation.core.tween(durationMillis = 1)
                } else {
                    motionConfig.springSpec()
                }
            )
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != currentPage) {
            onPageChange(pagerState.settledPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(pagerState.currentPage, swipeDownNotifications) {
                if (pagerState.currentPage != 0 || !swipeDownNotifications) return@pointerInput
                val triggerDistance = 100.dp.toPx()
                val horizontalSlop = 24.dp.toPx()
                awaitEachGesture {
                    val down = awaitFirstDown(
                        pass = PointerEventPass.Initial,
                        requireUnconsumed = false
                    )
                    var totalX = 0f
                    var totalY = 0f
                    var horizontalGesture = false
                    var triggered = false
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break

                        val delta = change.positionChangeIgnoreConsumed()
                        totalX += delta.x
                        totalY += delta.y

                        if (
                            kotlin.math.abs(totalX) > horizontalSlop &&
                            kotlin.math.abs(totalX) > kotlin.math.abs(totalY) * 1.15f
                        ) {
                            horizontalGesture = true
                        }

                        if (!horizontalGesture && !triggered && totalY >= triggerDistance) {
                            triggered = true
                            expandNotificationPanel(context)
                        }
                        if (triggered) change.consume()
                    }
                }
            }
    ) {
        BrutalWallpaper(
            uriString = wallpaperUri,
            modifier = Modifier.fillMaxSize()
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            userScrollEnabled = true,
            key = { it }
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                content(page)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 10.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(
                            onClickLabel = when (index) {
                                0 -> "Open Home"
                                1 -> "Open Apps"
                                else -> "Open Live"
                            },
                            onClick = { onPageChange(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (pagerState.currentPage == index) BrutalColors.Yellow else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = BrutalColors.Yellow
                            )
                    )
                }
            }
            Text(
                text = when (pagerState.currentPage) {
                    0 -> "HOME"
                    1 -> "APPS"
                    else -> "LIVE"
                },
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = BrutalColors.Yellow
            )
        }
    }
}
