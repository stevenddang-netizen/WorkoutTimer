package com.steven.workouttimer.ui.screens.fullscreen

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.steven.workouttimer.service.TimerState
import com.steven.workouttimer.ui.components.ControlButtons
import com.steven.workouttimer.ui.components.TimerDisplay
import com.steven.workouttimer.ui.theme.GlassCardBackground
import com.steven.workouttimer.ui.theme.GlassGradientEnd
import com.steven.workouttimer.ui.theme.GlassGradientStart
import com.steven.workouttimer.ui.theme.LocalIsGlassmorphic
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FullScreenTimerScreen(
    timerStateFlow: StateFlow<TimerState>,
    onExitFullScreen: () -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    val timerState by timerStateFlow.collectAsState()
    var showControls by remember { mutableStateOf(true) }
    val view = LocalView.current
    val isGlassmorphic = LocalIsGlassmorphic.current

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    // Handle back button
    BackHandler {
        onExitFullScreen()
    }

    // Navigate back when timer completes
    LaunchedEffect(timerState.isComplete) {
        if (timerState.isComplete) {
            onExitFullScreen()
        }
    }

    // Keep screen on and enable immersive mode
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val originalFlags = window.attributes.flags

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enable immersive mode
        val windowInsetsController = WindowCompat.getInsetsController(window, view)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            // Restore original state
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val backgroundModifier = if (isGlassmorphic) {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(GlassGradientStart, GlassGradientEnd)
            )
        )
    } else {
        Modifier.background(Color.Black)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(backgroundModifier)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                showControls = !showControls
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Timer name
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = timerState.timerName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            TimerDisplay(
                currentSecond = timerState.currentSecond,
                totalSeconds = timerState.totalSeconds,
                currentMinute = timerState.currentMinute,
                totalMinutes = timerState.totalMinutes,
                isFullScreen = true,
                isInInitialCountdown = timerState.isInInitialCountdown,
                initialCountdownRemaining = timerState.initialCountdownRemaining,
                timerMode = timerState.timerMode,
                currentRepetition = timerState.currentRepetition,
                totalRepetitions = timerState.totalRepetitions,
                secondInRep = timerState.secondInRep,
                holdSeconds = timerState.holdSeconds,
                restSeconds = timerState.restSeconds,
                isHolding = timerState.isHolding
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Controls
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ControlButtons(
                    isRunning = timerState.isRunning && !timerState.isPaused,
                    onPlayPauseClick = onPlayPause,
                    onStopClick = onStop,
                    onFullScreenClick = onExitFullScreen,
                    showFullScreenButton = false
                )
            }
        }

        // Exit full screen button
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            FilledIconButton(
                onClick = onExitFullScreen,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isGlassmorphic) GlassCardBackground else Color.White.copy(alpha = 0.2f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FullscreenExit,
                    contentDescription = "Exit Full Screen",
                    tint = Color.White
                )
            }
        }

        // Tap hint
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Tap to hide controls",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}
