package com.steven.workouttimer.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.steven.workouttimer.MainActivity
import com.steven.workouttimer.R
import com.steven.workouttimer.WorkoutTimerApp
import com.steven.workouttimer.audio.AudioNotificationManager
import com.steven.workouttimer.data.db.AudioType
import com.steven.workouttimer.data.db.TimerMode
import com.steven.workouttimer.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimerState(
    val timerId: Long = 0,
    val timerName: String = "",
    val timerMode: TimerMode = TimerMode.WEIGHTLIFT,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val currentSecond: Int = 0,
    val totalSeconds: Int = 0,
    val currentMinute: Int = 0,
    val totalMinutes: Int = 0,
    val audioEnabled: Boolean = true,
    val audioType: AudioType = AudioType.BEEP,
    val countdownSeconds: Int = 3,
    val initialCountdownSeconds: Int = 0,
    val initialCountdownRemaining: Int = 0,
    val isInInitialCountdown: Boolean = false,
    val isComplete: Boolean = false,
    // Climbing mode specific
    val holdSeconds: Int = 7,
    val restSeconds: Int = 3,
    val totalRepetitions: Int = 6,
    val currentRepetition: Int = 0,
    val secondInRep: Int = 0,
    val isHolding: Boolean = true  // true = hold phase, false = rest phase
)

class TimerService : Service() {

    private val binder = TimerBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null
    private var audioManager: AudioNotificationManager? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        audioManager = AudioNotificationManager(this)
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        audioManager?.release()
        releaseWakeLock()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "StevenWorkoutTimer::TimerWakeLock"
        ).apply {
            acquire(120 * 60 * 1000L) // Max 2 hours
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    fun startTimer(
        timerId: Long,
        timerName: String,
        timerMode: TimerMode,
        totalMinutes: Int,
        audioEnabled: Boolean,
        audioType: AudioType,
        countdownSeconds: Int,
        initialCountdownSeconds: Int = 0,
        // Climbing mode parameters
        holdSeconds: Int = 7,
        restSeconds: Int = 3,
        totalRepetitions: Int = 6
    ) {
        val hasInitialCountdown = initialCountdownSeconds > 0
        val repetitionSeconds = holdSeconds + restSeconds

        val totalSeconds = when (timerMode) {
            TimerMode.WEIGHTLIFT -> totalMinutes * 60
            TimerMode.CLIMBING -> repetitionSeconds * totalRepetitions
        }

        _timerState.value = TimerState(
            timerId = timerId,
            timerName = timerName,
            timerMode = timerMode,
            isRunning = true,
            isPaused = false,
            currentSecond = 0,
            totalSeconds = totalSeconds,
            currentMinute = 0,
            totalMinutes = totalMinutes,
            audioEnabled = audioEnabled,
            audioType = audioType,
            countdownSeconds = countdownSeconds,
            initialCountdownSeconds = initialCountdownSeconds,
            initialCountdownRemaining = initialCountdownSeconds,
            isInInitialCountdown = hasInitialCountdown,
            isComplete = false,
            holdSeconds = holdSeconds,
            restSeconds = restSeconds,
            totalRepetitions = totalRepetitions,
            currentRepetition = 0,
            secondInRep = 0,
            isHolding = true
        )

        startForeground(NOTIFICATION_ID, createNotification())
        if (hasInitialCountdown) {
            startInitialCountdown()
        } else {
            when (timerMode) {
                TimerMode.WEIGHTLIFT -> startWorkoutCountdown()
                TimerMode.CLIMBING -> startClimbingCountdown()
            }
        }
    }

    private fun startInitialCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_timerState.value.initialCountdownRemaining > 0) {
                if (!_timerState.value.isPaused) {
                    val state = _timerState.value
                    val remaining = state.initialCountdownRemaining

                    // Play countdown audio only when within the configured countdown threshold
                    if (state.audioEnabled && remaining <= state.countdownSeconds) {
                        playAudioNotification(state.audioType, remaining, state.countdownSeconds)
                    }

                    updateNotification()
                    delay(1000)

                    _timerState.value = state.copy(
                        initialCountdownRemaining = remaining - 1
                    )
                } else {
                    delay(1000)
                }
            }

            // Initial countdown complete, start the workout
            val currentState = _timerState.value
            _timerState.value = currentState.copy(
                isInInitialCountdown = false
            )
            audioManager?.playDoubleBeep()
            when (currentState.timerMode) {
                TimerMode.WEIGHTLIFT -> startWorkoutCountdown()
                TimerMode.CLIMBING -> startClimbingCountdown()
            }
        }
    }

    private fun startWorkoutCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_timerState.value.currentSecond < _timerState.value.totalSeconds) {
                if (!_timerState.value.isPaused) {
                    val state = _timerState.value
                    val currentSecond = state.currentSecond
                    val secondsInCurrentMinute = currentSecond % 60
                    val currentMinute = currentSecond / 60

                    // Check if we need to play audio notification
                    if (state.audioEnabled) {
                        val secondsUntilNextMinute = 60 - secondsInCurrentMinute
                        if (secondsUntilNextMinute <= state.countdownSeconds && secondsUntilNextMinute > 0) {
                            playAudioNotification(state.audioType, secondsUntilNextMinute, state.countdownSeconds)
                        }
                        // Play at the start of each minute (except first)
                        if (secondsInCurrentMinute == 0 && currentMinute > 0) {
                            audioManager?.playDoubleBeep()
                        }
                    }

                    _timerState.value = state.copy(
                        currentSecond = currentSecond + 1,
                        currentMinute = currentMinute
                    )

                    updateNotification()
                }
                delay(1000)
            }

            // Timer complete
            _timerState.value = _timerState.value.copy(
                isRunning = false,
                isComplete = true
            )
            audioManager?.speakText("Workout complete!")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun startClimbingCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_timerState.value.currentRepetition < _timerState.value.totalRepetitions) {
                if (!_timerState.value.isPaused) {
                    val state = _timerState.value
                    val secondInRep = state.secondInRep
                    val isHolding = secondInRep < state.holdSeconds
                    val repetitionSeconds = state.holdSeconds + state.restSeconds

                    // Calculate time remaining in current phase
                    val timeInPhase = if (isHolding) {
                        secondInRep
                    } else {
                        secondInRep - state.holdSeconds
                    }
                    val phaseLength = if (isHolding) state.holdSeconds else state.restSeconds
                    val secondsUntilNextPhase = phaseLength - timeInPhase

                    // Play audio notifications
                    if (state.audioEnabled) {
                        // Countdown before phase change (only during rest/break phase, not hold)
                        if (!isHolding && secondsUntilNextPhase <= state.countdownSeconds && secondsUntilNextPhase > 0) {
                            playAudioNotification(state.audioType, secondsUntilNextPhase, state.countdownSeconds)
                        }
                        // Announce phase change
                        if (secondInRep == 0 && state.currentRepetition > 0) {
                            audioManager?.playDoubleBeep()
                        }
                        if (secondInRep == state.holdSeconds && state.restSeconds > 0) {
                            audioManager?.speakText("Rest")
                        }
                    }

                    val nextSecondInRep = secondInRep + 1
                    val repComplete = nextSecondInRep >= repetitionSeconds

                    _timerState.value = state.copy(
                        currentSecond = state.currentSecond + 1,
                        secondInRep = if (repComplete) 0 else nextSecondInRep,
                        currentRepetition = if (repComplete) state.currentRepetition + 1 else state.currentRepetition,
                        isHolding = if (repComplete) true else nextSecondInRep < state.holdSeconds
                    )

                    updateNotification()
                }
                delay(1000)
            }

            // Climbing workout complete
            _timerState.value = _timerState.value.copy(
                isRunning = false,
                isComplete = true
            )
            audioManager?.speakText("Climbing workout complete!")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun playAudioNotification(audioType: AudioType, secondsRemaining: Int, maxCountdownSeconds: Int) {
        when (audioType) {
            AudioType.BEEP -> audioManager?.playCountdownBeep(secondsRemaining, maxCountdownSeconds)
            AudioType.VOICE -> audioManager?.speakNumber(secondsRemaining)
        }
    }

    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isPaused = true)
        updateNotification()
    }

    fun resumeTimer() {
        val state = _timerState.value
        _timerState.value = state.copy(isPaused = false)
        updateNotification()

        // Restart the appropriate countdown if job was cancelled
        if (timerJob?.isActive != true) {
            if (state.isInInitialCountdown) {
                startInitialCountdown()
            } else {
                when (state.timerMode) {
                    TimerMode.WEIGHTLIFT -> startWorkoutCountdown()
                    TimerMode.CLIMBING -> startClimbingCountdown()
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val state = _timerState.value
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_TIMER_ID, state.timerId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, TimerService::class.java).apply {
                action = if (state.isPaused) ACTION_RESUME else ACTION_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, TimerService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = when {
            state.isInInitialCountdown -> {
                if (state.isPaused) {
                    "Paused • Get ready: ${state.initialCountdownRemaining}s"
                } else {
                    "Get ready: ${state.initialCountdownRemaining}s"
                }
            }
            state.isPaused -> {
                val remainingSeconds = state.totalSeconds - state.currentSecond
                val timeDisplay = TimeUtils.formatTimeWithHours(remainingSeconds)
                "Paused • $timeDisplay remaining"
            }
            state.timerMode == TimerMode.CLIMBING -> {
                val phase = if (state.isHolding) "HOLD" else "REST"
                val repInfo = "Rep ${state.currentRepetition + 1}/${state.totalRepetitions}"
                val repetitionSeconds = state.holdSeconds + state.restSeconds
                val phaseTime = if (state.isHolding) {
                    state.holdSeconds - state.secondInRep
                } else {
                    repetitionSeconds - state.secondInRep
                }
                "$repInfo • $phase ${phaseTime}s"
            }
            else -> {
                val remainingSeconds = state.totalSeconds - state.currentSecond
                val timeDisplay = TimeUtils.formatTimeWithHours(remainingSeconds)
                val minuteInfo = "Minute ${state.currentMinute + 1}/${state.totalMinutes}"
                "$minuteInfo • $timeDisplay remaining"
            }
        }

        return NotificationCompat.Builder(this, WorkoutTimerApp.TIMER_CHANNEL_ID)
            .setContentTitle(state.timerName)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                if (state.isPaused) R.drawable.ic_play else R.drawable.ic_pause,
                if (state.isPaused) "Resume" else "Pause",
                pauseResumeIntent
            )
            .addAction(R.drawable.ic_stop, "Stop", stopIntent)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_PAUSE = "com.steven.workouttimer.PAUSE"
        const val ACTION_RESUME = "com.steven.workouttimer.RESUME"
        const val ACTION_STOP = "com.steven.workouttimer.STOP"
        const val EXTRA_TIMER_ID = "timer_id"
    }
}
