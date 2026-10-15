package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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
import java.util.Locale

@Composable
internal fun SettingsActionButton(
    title: String,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val normalizedTitle = title.uppercase(Locale.getDefault())
    val destructive = background == BrutalColors.Pink ||
        normalizedTitle.startsWith("CLEAR") || normalizedTitle.contains("RESET") ||
        normalizedTitle == "DELETE" || normalizedTitle.startsWith("REMOVE")
    val neutral = !destructive && normalizedTitle == "CANCEL"
    val resolvedBackground = when {
        destructive -> BrutalColors.Pink
        neutral -> surface
        else -> BrutalColors.Yellow
    }
    BrutalPressableBlock(
        modifier = modifier.fillMaxWidth(),
        background = resolvedBackground,
        borderWidth = 2.dp,
        shadowX = 0.dp,
        shadowY = 0.dp,
        onClick = onClick
    ) {
        Text(
            text = title.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (neutral) onSurface else BrutalColors.Ink,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
internal fun SettingsSectionTitle(title: String) {
    val readableTitle = title.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(BrutalColors.Yellow)
                .border(1.dp, MaterialTheme.colorScheme.onSurface)
        )
        Text(
            text = readableTitle,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun SettingsOptionButton(
    label: String,
    selected: Boolean,
    background: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    BrutalPressableBlock(
        modifier = modifier.heightIn(min = 42.dp),
        background = if (selected) {
            if (label in setOf("NEO BRUTAL DEFAULT", "VIOLET GRID")) {
                background
            } else {
                BrutalColors.Yellow
            }
        } else {
            MaterialTheme.colorScheme.surface
        },
        borderWidth = 2.dp,
        shadowX = 0.dp,
        shadowY = 0.dp,
        onClick = onClick
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) BrutalColors.Ink else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    background: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    BrutalBlock(
        modifier = Modifier.fillMaxWidth(),
        background = MaterialTheme.colorScheme.surface,
        borderWidth = 2.dp,
        shadowX = 0.dp,
        shadowY = 0.dp,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } },
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            BrutalToggle(
                checked = checked,
                accent = background,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
internal fun ThemeButton(
    label: String,
    selected: Boolean,
    background: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    BrutalPressableBlock(
        modifier = modifier,
        background = if (selected) background else MaterialTheme.colorScheme.surface,
        borderWidth = if (selected) NeoBrutalTokens.Border.Strong else NeoBrutalTokens.Border.Secondary,
        shadowX = NeoBrutalTokens.Shadow.Medium,
        shadowY = NeoBrutalTokens.Shadow.Medium,
        onClick = onClick
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = BrutalTypography.Display,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = if (selected) BrutalColors.Ink else MaterialTheme.colorScheme.onSurface
        )
    }
}
