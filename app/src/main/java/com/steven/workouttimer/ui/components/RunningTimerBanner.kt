package com.steven.workouttimer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.steven.workouttimer.service.TimerState
import com.steven.workouttimer.ui.theme.GlassBorder
import com.steven.workouttimer.ui.theme.GlassCardBackground
import com.steven.workouttimer.ui.theme.GlassPrimary
import com.steven.workouttimer.ui.theme.LocalIsGlassmorphic
import com.steven.workouttimer.util.TimeUtils

@Composable
fun RunningTimerBanner(
    timerState: TimerState,
    onTap: () -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingSeconds = timerState.totalSeconds - timerState.currentSecond
    val isGlassmorphic = LocalIsGlassmorphic.current

    // Pulsing animation for the indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGlassmorphic) GlassCardBackground else MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isGlassmorphic) 0.dp else 4.dp
        ),
        border = if (isGlassmorphic) BorderStroke(1.dp, GlassBorder) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pulsing indicator
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .alpha(if (timerState.isPaused) 0.5f else alpha)
                        .clip(CircleShape)
                        .background(
                            if (timerState.isPaused) {
                                if (isGlassmorphic) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                            } else {
                                if (isGlassmorphic) GlassPrimary else MaterialTheme.colorScheme.primary
                            }
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = timerState.timerName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isGlassmorphic) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (timerState.isPaused) {
                            "Paused • ${TimeUtils.formatTimeWithHours(remainingSeconds)} left"
                        } else {
                            "Minute ${timerState.currentMinute + 1}/${timerState.totalMinutes} • ${TimeUtils.formatTimeWithHours(remainingSeconds)} left"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isGlassmorphic) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Play/Pause button
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (timerState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (timerState.isPaused) "Resume" else "Pause",
                        tint = if (isGlassmorphic) GlassPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Stop button
                IconButton(onClick = onStop) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = if (isGlassmorphic) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete timer",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
