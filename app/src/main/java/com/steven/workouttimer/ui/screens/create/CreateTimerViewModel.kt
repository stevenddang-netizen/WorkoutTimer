package com.steven.workouttimer.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.steven.workouttimer.data.db.AudioType
import com.steven.workouttimer.data.db.TimerEntity
import com.steven.workouttimer.data.db.TimerMode
import com.steven.workouttimer.data.repository.TimerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateTimerUiState(
    val id: Long? = null,
    val name: String = "",
    val timerMode: TimerMode = TimerMode.WEIGHTLIFT,
    val totalMinutes: Int = 10,
    val audioEnabled: Boolean = true,
    val audioType: AudioType = AudioType.BEEP,
    val countdownSeconds: Int = 3,
    val initialCountdownSeconds: Int = 0,
    // Climbing mode specific
    val holdSeconds: Int = 7,
    val restSeconds: Int = 3,
    val totalRepetitions: Int = 6,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: String? = null
)

class CreateTimerViewModel(
    private val repository: TimerRepository,
    private val timerId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTimerUiState())
    val uiState: StateFlow<CreateTimerUiState> = _uiState.asStateFlow()

    init {
        if (timerId != null && timerId > 0) {
            loadTimer(timerId)
        }
    }

    private fun loadTimer(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getTimerById(id)?.let { timer ->
                _uiState.update {
                    it.copy(
                        id = timer.id,
                        name = timer.name,
                        timerMode = TimerMode.valueOf(timer.timerMode),
                        totalMinutes = timer.totalMinutes,
                        audioEnabled = timer.audioEnabled,
                        audioType = AudioType.valueOf(timer.audioType),
                        countdownSeconds = timer.countdownSeconds,
                        initialCountdownSeconds = timer.initialCountdownSeconds,
                        holdSeconds = timer.holdSeconds,
                        restSeconds = timer.restSeconds,
                        totalRepetitions = timer.totalRepetitions,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = if (name.isBlank()) "Name is required" else null
            )
        }
    }

    fun updateTotalMinutes(minutes: Int) {
        _uiState.update { it.copy(totalMinutes = minutes.coerceIn(2, 120)) }
    }

    fun updateAudioEnabled(enabled: Boolean) {
        _uiState.update { it.copy(audioEnabled = enabled) }
    }

    fun updateAudioType(type: AudioType) {
        _uiState.update { it.copy(audioType = type) }
    }

    fun updateCountdownSeconds(seconds: Int) {
        _uiState.update { it.copy(countdownSeconds = seconds.coerceIn(1, 10)) }
    }

    fun updateInitialCountdownSeconds(seconds: Int) {
        _uiState.update { it.copy(initialCountdownSeconds = seconds.coerceIn(0, 30)) }
    }

    fun updateTimerMode(mode: TimerMode) {
        _uiState.update { it.copy(timerMode = mode) }
    }

    fun updateHoldSeconds(seconds: Int) {
        _uiState.update { it.copy(holdSeconds = seconds.coerceIn(1, 60)) }
    }

    fun updateRestSeconds(seconds: Int) {
        _uiState.update { it.copy(restSeconds = seconds.coerceIn(1, 60)) }
    }

    fun updateTotalRepetitions(reps: Int) {
        _uiState.update { it.copy(totalRepetitions = reps.coerceIn(1, 100)) }
    }

    fun saveTimer() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            return
        }

        viewModelScope.launch {
            val timer = TimerEntity(
                id = state.id ?: 0,
                name = state.name.trim(),
                timerMode = state.timerMode.name,
                totalMinutes = state.totalMinutes,
                audioEnabled = state.audioEnabled,
                audioType = state.audioType.name,
                countdownSeconds = state.countdownSeconds,
                initialCountdownSeconds = state.initialCountdownSeconds,
                holdSeconds = state.holdSeconds,
                restSeconds = state.restSeconds,
                totalRepetitions = state.totalRepetitions
            )

            if (state.id != null) {
                repository.updateTimer(timer)
            } else {
                repository.insertTimer(timer)
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }

    companion object {
        fun factory(repository: TimerRepository, timerId: Long?): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CreateTimerViewModel(repository, timerId) as T
                }
            }
        }
    }
}
