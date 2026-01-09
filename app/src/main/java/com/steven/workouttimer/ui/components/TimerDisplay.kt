package com.steven.workouttimer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.steven.workouttimer.data.db.TimerMode
import com.steven.workouttimer.ui.theme.LocalIsGlassmorphic
import com.steven.workouttimer.util.TimeUtils

@Composable
fun TimerDisplay(
    currentSecond: Int,
    totalSeconds: Int,
    currentMinute: Int,
    totalMinutes: Int,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false,
    isInInitialCountdown: Boolean = false,
    initialCountdownRemaining: Int = 0,
    // Climbing mode parameters
    timerMode: TimerMode = TimerMode.WEIGHTLIFT,
    currentRepetition: Int = 0,
    totalRepetitions: Int = 0,
    secondInRep: Int = 0,
    holdSeconds: Int = 0,
    restSeconds: Int = 0,
    isHolding: Boolean = true
) {
    if (isInInitialCountdown) {
        InitialCountdownDisplay(
            secondsRemaining = initialCountdownRemaining,
            modifier = modifier,
            isFullScreen = isFullScreen
        )
    } else if (timerMode == TimerMode.CLIMBING) {
        ClimbingTimerDisplay(
            currentRepetition = currentRepetition,
            totalRepetitions = totalRepetitions,
            secondInRep = secondInRep,
            holdSeconds = holdSeconds,
            restSeconds = restSeconds,
            isHolding = isHolding,
            totalSeconds = totalSeconds,
            currentSecond = currentSecond,
            modifier = modifier,
            isFullScreen = isFullScreen
        )
    } else {
        WorkoutTimerDisplay(
            currentSecond = currentSecond,
            totalSeconds = totalSeconds,
            currentMinute = currentMinute,
            totalMinutes = totalMinutes,
            modifier = modifier,
            isFullScreen = isFullScreen
        )
    }
}

@Composable
private fun InitialCountdownDisplay(
    secondsRemaining: Int,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false
) {
    val isGlassmorphic = LocalIsGlassmorphic.current
    val textColor = if (isGlassmorphic) Color.White else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Get Ready",
            style = if (isFullScreen) {
                MaterialTheme.typography.headlineLarge
            } else {
                MaterialTheme.typography.headlineMedium
            },
            color = textColor.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(if (isFullScreen) 24.dp else 16.dp))

        Text(
            text = "$secondsRemaining",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = if (isFullScreen) 180.sp else 96.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFFFF9800) // Orange for countdown
        )

        Spacer(modifier = Modifier.height(if (isFullScreen) 24.dp else 16.dp))

        Text(
            text = "Workout starting soon...",
            style = if (isFullScreen) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.titleMedium
            },
            color = textColor.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun WorkoutTimerDisplay(
    currentSecond: Int,
    totalSeconds: Int,
    currentMinute: Int,
    totalMinutes: Int,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false
) {
    val isGlassmorphic = LocalIsGlassmorphic.current
    val textColor = if (isGlassmorphic) Color.White else MaterialTheme.colorScheme.onBackground

    val secondsInCurrentMinute = 60 - (currentSecond % 60)
    val displaySeconds = if (currentSecond % 60 == 0) 60 else secondsInCurrentMinute

    val timerColor = when {
        displaySeconds <= 3 -> Color(0xFFF44336) // Red
        displaySeconds <= 10 -> Color(0xFFFFEB3B) // Yellow
        isGlassmorphic -> Color(0xFF7ECFA0) // Mint green for glassmorphic
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
            color = textColor.copy(alpha = 0.7f)
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
            color = textColor.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ClimbingTimerDisplay(
    currentRepetition: Int,
    totalRepetitions: Int,
    secondInRep: Int,
    holdSeconds: Int,
    restSeconds: Int,
    isHolding: Boolean,
    totalSeconds: Int,
    currentSecond: Int,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false
) {
    val isGlassmorphic = LocalIsGlassmorphic.current
    val textColor = if (isGlassmorphic) Color.White else MaterialTheme.colorScheme.onBackground

    val timeInPhase = if (isHolding) secondInRep else secondInRep - holdSeconds
    val phaseLength = if (isHolding) holdSeconds else restSeconds
    val secondsRemaining = phaseLength - timeInPhase

    val timerColor = when {
        isHolding -> Color(0xFF4CAF50) // Green for hold
        else -> Color(0xFF2196F3) // Blue for rest
    }

    val urgencyColor = when {
        secondsRemaining <= 3 -> Color(0xFFF44336) // Red
        secondsRemaining <= 5 -> Color(0xFFFFEB3B) // Yellow
        else -> timerColor
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Current rep indicator
        Text(
            text = "Rep ${currentRepetition + 1} of $totalRepetitions",
            style = if (isFullScreen) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            color = textColor.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(if (isFullScreen) 16.dp else 8.dp))

        // Phase indicator
        Text(
            text = if (isHolding) "HOLD" else "REST",
            style = if (isFullScreen) {
                MaterialTheme.typography.headlineLarge
            } else {
                MaterialTheme.typography.headlineMedium
            },
            fontWeight = FontWeight.Bold,
            color = timerColor
        )

        Spacer(modifier = Modifier.height(if (isFullScreen) 16.dp else 8.dp))

        // Main timer display - seconds remaining in current phase
        Text(
            text = "$secondsRemaining",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = if (isFullScreen) 180.sp else 96.sp,
                fontWeight = FontWeight.Bold
            ),
            color = urgencyColor
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
            color = textColor.copy(alpha = 0.5f)
        )
    }
}
