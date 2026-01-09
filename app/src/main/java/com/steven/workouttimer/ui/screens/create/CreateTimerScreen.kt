package com.steven.workouttimer.ui.screens.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.steven.workouttimer.data.db.AudioType
import com.steven.workouttimer.data.db.TimerMode
import com.steven.workouttimer.ui.theme.GlassSurface
import com.steven.workouttimer.ui.theme.LocalIsGlassmorphic
import com.steven.workouttimer.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTimerScreen(
    viewModel: CreateTimerViewModel,
    onNavigateBack: () -> Unit,
    isEditing: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val isGlassmorphic = LocalIsGlassmorphic.current
    val textColor = if (isGlassmorphic) Color.White else MaterialTheme.colorScheme.onBackground
    val subtextColor = if (isGlassmorphic) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = if (isGlassmorphic) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Timer" else "Create Timer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveTimer() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isGlassmorphic) GlassSurface else MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = if (isGlassmorphic) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = if (isGlassmorphic) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = if (isGlassmorphic) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Timer Mode Selection
            Column {
                Text(
                    text = "Timer Mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                var modeExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = modeExpanded,
                    onExpandedChange = { modeExpanded = !modeExpanded }
                ) {
                    OutlinedTextField(
                        value = when (uiState.timerMode) {
                            TimerMode.WEIGHTLIFT -> "Weightlift Mode"
                            TimerMode.CLIMBING -> "Climbing Mode"
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = modeExpanded,
                        onDismissRequest = { modeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Weightlift Mode") },
                            onClick = {
                                viewModel.updateTimerMode(TimerMode.WEIGHTLIFT)
                                modeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Climbing Mode") },
                            onClick = {
                                viewModel.updateTimerMode(TimerMode.CLIMBING)
                                modeExpanded = false
                            }
                        )
                    }
                }
            }

            // Timer Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Timer Name") },
                placeholder = {
                    Text(
                        when (uiState.timerMode) {
                            TimerMode.WEIGHTLIFT -> "e.g., Morning EMOM"
                            TimerMode.CLIMBING -> "e.g., Hangboard Session"
                        }
                    )
                },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Mode-specific options
            if (uiState.timerMode == TimerMode.WEIGHTLIFT) {
                // Duration (Weightlift mode)
                Column {
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    val minuteOptions = (2..120).toList()

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = TimeUtils.formatMinutes(uiState.totalMinutes),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            minuteOptions.forEach { minutes ->
                                DropdownMenuItem(
                                    text = { Text(TimeUtils.formatMinutes(minutes)) },
                                    onClick = {
                                        viewModel.updateTotalMinutes(minutes)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Climbing mode options

                // Hold Length
                Column {
                    Text(
                        text = "Hold Length",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                    Text(
                        text = "How long to hold each rep",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var holdExpanded by remember { mutableStateOf(false) }
                    val holdOptions = (1..60).toList()

                    ExposedDropdownMenuBox(
                        expanded = holdExpanded,
                        onExpandedChange = { holdExpanded = !holdExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${uiState.holdSeconds} seconds",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = holdExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = holdExpanded,
                            onDismissRequest = { holdExpanded = false }
                        ) {
                            holdOptions.forEach { seconds ->
                                DropdownMenuItem(
                                    text = { Text("$seconds seconds") },
                                    onClick = {
                                        viewModel.updateHoldSeconds(seconds)
                                        holdExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Break Length
                Column {
                    Text(
                        text = "Break Length",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                    Text(
                        text = "Rest time between holds",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var restExpanded by remember { mutableStateOf(false) }
                    val restOptions = (1..60).toList()

                    ExposedDropdownMenuBox(
                        expanded = restExpanded,
                        onExpandedChange = { restExpanded = !restExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${uiState.restSeconds} seconds",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = restExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = restExpanded,
                            onDismissRequest = { restExpanded = false }
                        ) {
                            restOptions.forEach { seconds ->
                                DropdownMenuItem(
                                    text = { Text("$seconds seconds") },
                                    onClick = {
                                        viewModel.updateRestSeconds(seconds)
                                        restExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Total Repetitions
                Column {
                    Text(
                        text = "Total Repetitions",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                    Text(
                        text = "Number of reps to complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var repsExpanded by remember { mutableStateOf(false) }
                    val repsOptions = (1..50).toList()

                    ExposedDropdownMenuBox(
                        expanded = repsExpanded,
                        onExpandedChange = { repsExpanded = !repsExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${uiState.totalRepetitions} reps",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repsExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = repsExpanded,
                            onDismissRequest = { repsExpanded = false }
                        ) {
                            repsOptions.forEach { reps ->
                                DropdownMenuItem(
                                    text = { Text("$reps reps") },
                                    onClick = {
                                        viewModel.updateTotalRepetitions(reps)
                                        repsExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Initial Countdown
            Column {
                Text(
                    text = "Initial Countdown",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Text(
                    text = "Countdown before workout starts",
                    style = MaterialTheme.typography.bodySmall,
                    color = subtextColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                var initialCountdownExpanded by remember { mutableStateOf(false) }
                val initialCountdownOptions = listOf(0) + (2..30).toList()

                ExposedDropdownMenuBox(
                    expanded = initialCountdownExpanded,
                    onExpandedChange = { initialCountdownExpanded = !initialCountdownExpanded }
                ) {
                    OutlinedTextField(
                        value = if (uiState.initialCountdownSeconds == 0) "None" else "${uiState.initialCountdownSeconds} seconds",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = initialCountdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = initialCountdownExpanded,
                        onDismissRequest = { initialCountdownExpanded = false }
                    ) {
                        initialCountdownOptions.forEach { seconds ->
                            DropdownMenuItem(
                                text = { Text(if (seconds == 0) "None" else "$seconds seconds") },
                                onClick = {
                                    viewModel.updateInitialCountdownSeconds(seconds)
                                    initialCountdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Audio Notification Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Audio Notification",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                    Text(
                        text = when (uiState.timerMode) {
                            TimerMode.WEIGHTLIFT -> "Play sound before each minute"
                            TimerMode.CLIMBING -> "Play sound before each rep"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                }
                Switch(
                    checked = uiState.audioEnabled,
                    onCheckedChange = { viewModel.updateAudioEnabled(it) }
                )
            }

            // Audio Type Selection (only visible when audio is enabled)
            if (uiState.audioEnabled) {
                Column {
                    Text(
                        text = "Notification Type",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.audioType == AudioType.BEEP,
                            onClick = { viewModel.updateAudioType(AudioType.BEEP) },
                            label = { Text("Beep") }
                        )
                        FilterChip(
                            selected = uiState.audioType == AudioType.VOICE,
                            onClick = { viewModel.updateAudioType(AudioType.VOICE) },
                            label = { Text("Voice Countdown") }
                        )
                    }
                }

                // Countdown Seconds
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start Notification At",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor
                        )
                        Text(
                            text = "${uiState.countdownSeconds} seconds before",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isGlassmorphic) Color(0xFF7ECFA0) else MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = uiState.countdownSeconds.toFloat(),
                        onValueChange = { viewModel.updateCountdownSeconds(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1 sec",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtextColor
                        )
                        Text(
                            text = "10 sec",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info text
            Text(
                text = when (uiState.timerMode) {
                    TimerMode.WEIGHTLIFT -> "EMOM (Every Minute On the Minute): A workout where you start a new exercise at the beginning of each minute. This timer will track your progress and notify you before each new minute begins."
                    TimerMode.CLIMBING -> "Climbing Mode: Designed for hangboard and climbing training. Each rep consists of a hold period followed by rest. The timer will notify you before each new rep begins."
                },
                style = MaterialTheme.typography.bodySmall,
                color = subtextColor
            )
        }
    }
}
