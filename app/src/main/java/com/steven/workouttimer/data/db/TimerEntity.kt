package com.steven.workouttimer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AudioType {
    BEEP,
    VOICE
}

enum class TimerMode {
    WEIGHTLIFT,
    CLIMBING
}

@Entity(tableName = "timers")
data class TimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val timerMode: String = TimerMode.WEIGHTLIFT.name,
    val totalMinutes: Int,
    val audioEnabled: Boolean = true,
    val audioType: String = AudioType.BEEP.name,
    val countdownSeconds: Int = 3,
    val initialCountdownSeconds: Int = 0,
    // Climbing mode specific fields
    val holdSeconds: Int = 7,         // Hold length (1-60 seconds)
    val restSeconds: Int = 3,         // Break/rest length (1-60 seconds)
    val totalRepetitions: Int = 6,    // Number of repetitions
    val createdAt: Long = System.currentTimeMillis()
)
