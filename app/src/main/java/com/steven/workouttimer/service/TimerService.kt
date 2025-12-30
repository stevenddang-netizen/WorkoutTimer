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
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val currentSecond: Int = 0,
    val totalSeconds: Int = 0,
    val currentMinute: Int = 0,
    val totalMinutes: Int = 0,
    val audioEnabled: Boolean = true,
    val audioType: AudioType = AudioType.BEEP,
    val countdownSeconds: Int = 3,
    val isComplete: Boolean = false
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
        totalMinutes: Int,
        audioEnabled: Boolean,
        audioType: AudioType,
        countdownSeconds: Int
    ) {
        val totalSeconds = totalMinutes * 60

        _timerState.value = TimerState(
            timerId = timerId,
            timerName = timerName,
            isRunning = true,
            isPaused = false,
            currentSecond = 0,
            totalSeconds = totalSeconds,
            currentMinute = 0,
            totalMinutes = totalMinutes,
            audioEnabled = audioEnabled,
            audioType = audioType,
            countdownSeconds = countdownSeconds,
            isComplete = false
        )

        startForeground(NOTIFICATION_ID, createNotification())
        startCountdown()
    }

    private fun startCountdown() {
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
                            playAudioNotification(state.audioType, secondsUntilNextMinute)
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

    private fun playAudioNotification(audioType: AudioType, secondsRemaining: Int) {
        when (audioType) {
            AudioType.BEEP -> audioManager?.playBeep()
            AudioType.VOICE -> audioManager?.speakNumber(secondsRemaining)
        }
    }

    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isPaused = true)
        updateNotification()
    }

    fun resumeTimer() {
        _timerState.value = _timerState.value.copy(isPaused = false)
        updateNotification()
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

        val remainingSeconds = state.totalSeconds - state.currentSecond
        val timeDisplay = TimeUtils.formatTimeWithHours(remainingSeconds)
        val minuteInfo = "Minute ${state.currentMinute + 1}/${state.totalMinutes}"

        val contentText = if (state.isPaused) {
            "Paused • $timeDisplay remaining"
        } else {
            "$minuteInfo • $timeDisplay remaining"
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
