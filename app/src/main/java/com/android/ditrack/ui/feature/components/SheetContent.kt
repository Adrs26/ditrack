package com.android.ditrack.ui.feature.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.ditrack.R
import com.android.ditrack.domain.model.ApplicationMode

@Composable
fun SheetContent(
    applicationMode: ApplicationMode,
    originName: String,
    destinationName: String,
    duration: String,
    distance: String,
    onExitWaiting: () -> Unit,
    onChangeDestination: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        SheetMainInformationContent(
            duration = duration,
            applicationMode = applicationMode
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.tertiary
        )
        if (applicationMode == ApplicationMode.WAITING) {
            SheetBusInformationContent(
                modifier = Modifier.padding(vertical = 24.dp)
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 8.dp)
        ) {
            SheetRouteInformationContent(
                applicationMode = applicationMode,
                originName = originName,
                destinationName = destinationName,
                distance = distance,
                onChangeDestination = onChangeDestination,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            if (applicationMode == ApplicationMode.WAITING) {
                OutlinedButton(
                    onClick = onExitWaiting,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = stringResource(R.string.exit_waiting_mode),
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SheetHandle(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp, 4.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.secondary)
        )
    }
}