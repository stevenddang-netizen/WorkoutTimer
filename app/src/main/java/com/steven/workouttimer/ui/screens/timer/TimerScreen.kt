package com.steven.workouttimer.ui.screens.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.steven.workouttimer.ui.components.ControlButtons
import com.steven.workouttimer.ui.components.TimerDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    onNavigateBack: () -> Unit,
    onFullScreen: () -> Unit
) {
    val timerState by viewModel.timerState.collectAsState()
    val timer by viewModel.timer.collectAsState()
    var showStopConfirmation by remember { mutableStateOf(false) }

    // Navigate back when timer completes
    LaunchedEffect(timerState.isComplete) {
        if (timerState.isComplete) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(timer?.name ?: "Timer") },
                navigationIcon = {
                    // Back button just navigates back, timer continues in background
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                TimerDisplay(
                    currentSecond = timerState.currentSecond,
                    totalSeconds = timerState.totalSeconds,
                    currentMinute = timerState.currentMinute,
                    totalMinutes = timerState.totalMinutes
                )

                Spacer(modifier = Modifier.height(48.dp))

                ControlButtons(
                    isRunning = timerState.isRunning && !timerState.isPaused,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onStopClick = { showStopConfirmation = true },
                    onFullScreenClick = onFullScreen
                )
            }
        }
    }

    // Stop confirmation dialog (only for stop button)
    if (showStopConfirmation) {
        AlertDialog(
            onDismissRequest = { showStopConfirmation = false },
            title = { Text("Stop Timer") },
            text = { Text("Are you sure you want to stop this workout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.stopTimer()
                        showStopConfirmation = false
                        onNavigateBack()
                    }
                ) {
                    Text("Stop", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmation = false }) {
                    Text("Continue")
                }
            }
        )
    }
}
