package com.steven.workouttimer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.steven.workouttimer.data.db.AppDatabase
import com.steven.workouttimer.data.preferences.ThemePreferences
import com.steven.workouttimer.data.repository.TimerRepository

class WorkoutTimerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_CHANNEL_ID,
                getString(R.string.timer_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.timer_notification_channel_desc)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val TIMER_CHANNEL_ID = "timer_channel"
    }
}

class AppContainer(private val application: Application) {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(application)
    }

    val timerRepository: TimerRepository by lazy {
        TimerRepository(database.timerDao())
    }

    val themePreferences: ThemePreferences by lazy {
        ThemePreferences(application)
    }
}
