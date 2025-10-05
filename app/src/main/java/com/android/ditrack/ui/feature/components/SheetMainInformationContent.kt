package com.android.ditrack.ui.feature.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.ditrack.R
import com.android.ditrack.data.datastore.ApplicationMode
import com.android.ditrack.ui.theme.Blue800
import com.android.ditrack.ui.theme.Charcoal

@Composable
fun SheetMainInformationContent(
    applicationMode: ApplicationMode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_schedule),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Bus akan tiba ke lokasi dalam",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)
            )
            Text(
                text = "15 Menit",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ApplicationModeIcon(
                icon = R.drawable.ic_directions_walk,
                applicationMode = applicationMode
            )
            LinearProgress(
                status = 2,
                modifier = Modifier.weight(1f)
            )
            ApplicationModeIcon(
                icon = R.drawable.ic_departure_board_outlined,
                applicationMode = applicationMode
            )
            LinearProgress(
                status = if (applicationMode == ApplicationMode.WAITING) 3 else 2,
                modifier = Modifier.weight(1f)
            )
            ApplicationModeIcon(
                icon = R.drawable.ic_directions_bus,
                applicationMode = applicationMode
            )
            LinearProgress(
                status = when (applicationMode) {
                    ApplicationMode.DEFAULT -> 1
                    ApplicationMode.WAITING -> 1
                    ApplicationMode.DRIVING -> 3
                    ApplicationMode.ARRIVED -> 2
                },
                modifier = Modifier.weight(1f)
            )
            ApplicationModeIcon(
                icon = R.drawable.ic_store,
                applicationMode = applicationMode
            )
        }
    }
}

@Composable
private fun ApplicationModeIcon(
    icon: Int,
    applicationMode: ApplicationMode,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawCircle(
                    color = changeIconColor(icon, applicationMode),
                    radius = size.minDimension / 2 - strokeWidth / 2,
                    style = Stroke(width = strokeWidth)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = changeIconColor(icon, applicationMode)
        )
        if (changeIconColor(icon, applicationMode) == MaterialTheme.colorScheme.primary) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = null,
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.extraSmall
                    ),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LinearProgress(
    status: Int,
    modifier: Modifier = Modifier
) {
    val progress = rememberInfiniteTransition()
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

    LinearProgressIndicator(
        progress = {
            when (status) {
                1 -> 0f
                2 -> 1f
                else -> progress.value
            }
        },
        modifier = modifier.height(4.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.tertiary
    )
}

private fun changeIconColor(
    icon: Int,
    applicationMode: ApplicationMode
): Color {
    return when(icon) {
        R.drawable.ic_directions_bus -> {
            when(applicationMode) {
                ApplicationMode.DEFAULT -> Charcoal
                ApplicationMode.WAITING -> Charcoal
                ApplicationMode.DRIVING -> Blue800
                ApplicationMode.ARRIVED -> Blue800
            }
        }
        R.drawable.ic_store -> {
            when(applicationMode) {
                ApplicationMode.DEFAULT -> Charcoal
                ApplicationMode.WAITING -> Charcoal
                ApplicationMode.DRIVING -> Charcoal
                ApplicationMode.ARRIVED -> Blue800
            }
        }
        else -> Blue800
    }
}