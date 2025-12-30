package com.steven.workouttimer.ui.screens.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.steven.workouttimer.data.db.AudioType
import com.steven.workouttimer.data.db.TimerEntity
import com.steven.workouttimer.data.repository.TimerRepository
import com.steven.workouttimer.service.TimerService
import com.steven.workouttimer.service.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel(
    private val context: Context,
    private val repository: TimerRepository,
    private val timerId: Long
) : ViewModel() {

    private var timerService: TimerService? = null
    private var bound = false

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _timer = MutableStateFlow<TimerEntity?>(null)
    val timer: StateFlow<TimerEntity?> = _timer.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            bound = true

            // Collect timer state from service
            viewModelScope.launch {
                timerService?.timerState?.collect { state ->
                    _timerState.value = state
                }
            }

            // Start the timer if not already running
            val currentTimer = _timer.value
            if (currentTimer != null && !timerService!!.timerState.value.isRunning) {
                timerService?.startTimer(
                    timerId = currentTimer.id,
                    timerName = currentTimer.name,
                    totalMinutes = currentTimer.totalMinutes,
                    audioEnabled = currentTimer.audioEnabled,
                    audioType = AudioType.valueOf(currentTimer.audioType),
                    countdownSeconds = currentTimer.countdownSeconds
                )
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            bound = false
        }
    }

    init {
        loadTimer()
    }

    private fun loadTimer() {
        viewModelScope.launch {
            repository.getTimerById(timerId)?.let { timer ->
                _timer.value = timer
                startService()
            }
        }
    }

    private fun startService() {
        val intent = Intent(context, TimerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun togglePlayPause() {
        timerService?.let { service ->
            if (service.timerState.value.isPaused) {
                service.resumeTimer()
            } else {
                service.pauseTimer()
            }
        }
    }

    fun stopTimer() {
        timerService?.stopTimer()
    }

    override fun onCleared() {
        super.onCleared()
        if (bound) {
            context.unbindService(connection)
            bound = false
        }
    }

    companion object {
        fun factory(
            context: Context,
            repository: TimerRepository,
            timerId: Long
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TimerViewModel(context, repository, timerId) as T
                }
            }
        }
    }
}
