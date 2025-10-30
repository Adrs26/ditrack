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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DepartureBoard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.ditrack.domain.common.ApplicationModeState
import com.android.ditrack.ui.theme.Blue800
import com.android.ditrack.ui.theme.Charcoal

@Composable
fun SheetMainInformationContent(
    applicationModeState: ApplicationModeState,
    duration: Int,
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
                imageVector = Icons.Default.AccessTimeFilled,
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
                text = "$duration menit",
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
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                applicationModeState = applicationModeState
            )
            LinearProgress(
                status = 2,
                modifier = Modifier.weight(1f)
            )
            ApplicationModeIcon(
                icon = Icons.Default.DepartureBoard,
                applicationModeState = applicationModeState
            )
            LinearProgress(
                status = if (applicationModeState == ApplicationModeState.Wait) 3 else 2,
                modifier = Modifier.weight(1f)
            )
            ApplicationModeIcon(
                icon = Icons.Default.DirectionsBus,
                applicationModeState = applicationModeState
            )
            LinearProgress(
                status = when (applicationModeState) {
                    ApplicationModeState.Idle -> 1
                    ApplicationModeState.Wait -> 1
                    ApplicationModeState.Drive -> 3
                    ApplicationModeState.Arrive -> 2
                },
                modifier = Modifier.weight(1f)
            )
            ApplicationModeIcon(
                icon = Icons.Default.Store,
                applicationModeState = applicationModeState
            )
        }
    }
}

@Composable
private fun ApplicationModeIcon(
    icon: ImageVector,
    applicationModeState: ApplicationModeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawCircle(
                    color = changeIconColor(icon, applicationModeState),
                    radius = size.minDimension / 2 - strokeWidth / 2,
                    style = Stroke(width = strokeWidth)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = changeIconColor(icon, applicationModeState)
        )
        if (changeIconColor(icon, applicationModeState) == MaterialTheme.colorScheme.primary) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
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
    icon: ImageVector,
    applicationModeState: ApplicationModeState
): Color {
    return when(icon) {
        Icons.Default.DirectionsBus -> {
            when(applicationModeState) {
                ApplicationModeState.Idle -> Charcoal
                ApplicationModeState.Wait -> Charcoal
                ApplicationModeState.Drive -> Blue800
                ApplicationModeState.Arrive -> Blue800
            }
        }
        Icons.Default.Store -> {
            when(applicationModeState) {
                ApplicationModeState.Idle -> Charcoal
                ApplicationModeState.Wait -> Charcoal
                ApplicationModeState.Drive -> Charcoal
                ApplicationModeState.Arrive -> Blue800
            }
        }
        else -> Blue800
    }
}