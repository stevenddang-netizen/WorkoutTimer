package com.steven.workouttimer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.steven.workouttimer.util.TimeUtils

@Composable
fun TimerDisplay(
    currentSecond: Int,
    totalSeconds: Int,
    currentMinute: Int,
    totalMinutes: Int,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false
) {
    val secondsInCurrentMinute = 60 - (currentSecond % 60)
    val displaySeconds = if (currentSecond % 60 == 0) 60 else secondsInCurrentMinute

    val timerColor = when {
        displaySeconds <= 3 -> Color(0xFFF44336) // Red
        displaySeconds <= 10 -> Color(0xFFFFEB3B) // Yellow
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Current minute indicator
        Text(
            text = "Minute ${currentMinute + 1} of $totalMinutes",
            style = if (isFullScreen) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(if (isFullScreen) 24.dp else 16.dp))

        // Main timer display - seconds remaining in current minute
        Text(
            text = String.format(":%02d", if (displaySeconds == 60) 0 else displaySeconds),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = if (isFullScreen) 180.sp else 96.sp,
                fontWeight = FontWeight.Bold
            ),
            color = timerColor
        )

        Spacer(modifier = Modifier.height(if (isFullScreen) 24.dp else 16.dp))

        // Total time remaining
        Text(
            text = "Total: ${TimeUtils.formatTimeWithHours(totalSeconds - currentSecond)}",
            style = if (isFullScreen) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.titleMedium
            },
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}
