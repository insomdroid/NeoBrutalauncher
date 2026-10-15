package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
internal fun SettingsDialogs(
    showIconPackPicker: Boolean,
    onShowIconPackPickerChange: (Boolean) -> Unit,
    showPinnedAppPicker: Boolean,
    onShowPinnedAppPickerChange: (Boolean) -> Unit,
    showChatAppPicker: Boolean,
    onShowChatAppPickerChange: (Boolean) -> Unit,
    showResetConfirm: Boolean,
    onShowResetConfirmChange: (Boolean) -> Unit,
    iconPacks: List<IconPackInfo>,
    iconPackPackage: String?,
    onIconPackChange: (String?) -> Unit,
    uiBackground: Color,
    uiSurface: Color,
    uiOnSurface: Color,
    pinnedCandidates: List<AppInfo>,
    favorites: Set<String>,
    homeAppCount: Int,
    onTogglePinnedApp: (AppInfo) -> Unit,
    chatCandidates: List<AppInfo>,
    chatNotificationPackages: List<String>,
    onChatNotificationPackagesChange: (List<String>) -> Unit,
    onResetAll: () -> Unit
) {
    if (showIconPackPicker) {
        AlertDialog(
            onDismissRequest = { onShowIconPackPickerChange(false) },
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
                                    onShowIconPackPickerChange(false)
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
                                    onShowIconPackPickerChange(false)
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
            onDismissRequest = { onShowPinnedAppPickerChange(false) },
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
        AlertDialog(onDismissRequest = { onShowChatAppPickerChange(false) }, title = { Text("CHOOSE CHAT APP", fontFamily = BrutalTypography.Display, fontWeight = FontWeight.SemiBold) }, text = {
            LazyColumn(modifier = Modifier.heightIn(max = minOf(360.dp, (LocalConfiguration.current.screenHeightDp * 0.55f).dp)), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(chatCandidates, key = { it.packageName + "/" + it.activityName }) { app ->
                    BrutalBlock(Modifier.fillMaxWidth().clickable { onChatNotificationPackagesChange(listOf(app.packageName)); onShowChatAppPickerChange(false) }, background = if(app.packageName == chatNotificationPackages.firstOrNull()) BrutalColors.Yellow else uiSurface, borderWidth = 3.dp, shadowX = 3.dp, shadowY = 3.dp) {
                        Text(app.label.uppercase(Locale.ENGLISH), fontSize = 12.sp, fontWeight = FontWeight.Black, color = if(app.packageName == chatNotificationPackages.firstOrNull()) BrutalColors.Ink else uiOnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }, confirmButton = {})
    }

    if (showResetConfirm) {
        AlertDialog(onDismissRequest = { onShowResetConfirmChange(false) }, title = { Text("RESET ALL CUSTOMIZATIONS", fontFamily = BrutalTypography.Display) }, text = { Text("This clears launcher preferences, tile layout, pinned apps, notes/tasks, wallpaper and icon-pack selection. This cannot be undone.") }, dismissButton = { SettingsActionButton("CANCEL", BrutalColors.White) { onShowResetConfirmChange(false) } }, confirmButton = { SettingsActionButton("RESET", BrutalColors.Pink) { onShowResetConfirmChange(false); onResetAll() } })
    }

}
