package com.steven.workouttimer.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.steven.workouttimer.data.db.TimerEntity
import com.steven.workouttimer.data.preferences.ThemeMode
import com.steven.workouttimer.service.TimerState
import com.steven.workouttimer.ui.components.RunningTimerBanner
import com.steven.workouttimer.ui.components.SettingsDialog
import com.steven.workouttimer.ui.components.TimerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    runningTimerState: TimerState?,
    onRunningTimerTap: () -> Unit,
    onRunningTimerPlayPause: () -> Unit,
    onRunningTimerStop: () -> Unit,
    onCreateTimer: () -> Unit,
    onEditTimer: (Long) -> Unit,
    onStartTimer: (Long) -> Unit
) {
    val timers by viewModel.timers.collectAsState()
    var timerToDelete by remember { mutableStateOf<TimerEntity?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showStopConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Timers") },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTimer,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Timer"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Running timer banner
            if (runningTimerState != null && runningTimerState.isRunning) {
                RunningTimerBanner(
                    timerState = runningTimerState,
                    onTap = onRunningTimerTap,
                    onPlayPause = onRunningTimerPlayPause,
                    onStop = { showStopConfirmation = true },
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (timers.isEmpty()) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = if (runningTimerState?.isRunning == true) 0.dp else 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = timers,
                        key = { it.id }
                    ) { timer ->
                        TimerCard(
                            timer = timer,
                            onPlayClick = { onStartTimer(timer.id) },
                            onEditClick = { onEditTimer(timer.id) },
                            onDeleteClick = { timerToDelete = timer }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    timerToDelete?.let { timer ->
        AlertDialog(
            onDismissRequest = { timerToDelete = null },
            title = { Text("Delete Timer") },
            text = { Text("Are you sure you want to delete \"${timer.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTimer(timer)
                        timerToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { timerToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Settings dialog
    if (showSettings) {
        SettingsDialog(
            currentThemeMode = currentThemeMode,
            onThemeModeChange = onThemeModeChange,
            onDismiss = { showSettings = false }
        )
    }

    // Stop running timer confirmation dialog
    if (showStopConfirmation) {
        AlertDialog(
            onDismissRequest = { showStopConfirmation = false },
            title = { Text("Stop Timer") },
            text = { Text("Are you sure you want to stop this workout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRunningTimerStop()
                        showStopConfirmation = false
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

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
            Text(
                text = "No timers yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap + to create your first\nEMOM workout timer",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}
