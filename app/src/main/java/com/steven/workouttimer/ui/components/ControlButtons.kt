package com.steven.workouttimer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.steven.workouttimer.ui.theme.GlassCardBackground
import com.steven.workouttimer.ui.theme.GlassPrimary
import com.steven.workouttimer.ui.theme.LocalIsGlassmorphic

@Composable
fun ControlButtons(
    isRunning: Boolean,
    onPlayPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onFullScreenClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFullScreenButton: Boolean = true
) {
    val isGlassmorphic = LocalIsGlassmorphic.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stop button
        FilledIconButton(
            onClick = onStopClick,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isGlassmorphic) GlassCardBackground else MaterialTheme.colorScheme.error,
                contentColor = if (isGlassmorphic) Color(0xFFF44336) else Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop",
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Play/Pause button
        FilledIconButton(
            onClick = onPlayPauseClick,
            modifier = Modifier.size(80.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isGlassmorphic) GlassCardBackground else MaterialTheme.colorScheme.primary,
                contentColor = if (isGlassmorphic) GlassPrimary else Color.White
            )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "Pause" else "Play",
                modifier = Modifier.size(48.dp)
            )
        }

        if (showFullScreenButton) {
            Spacer(modifier = Modifier.width(24.dp))

            // Full screen button
            FilledIconButton(
                onClick = onFullScreenClick,
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isGlassmorphic) GlassCardBackground else MaterialTheme.colorScheme.secondary,
                    contentColor = if (isGlassmorphic) Color.White else Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Full Screen",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
