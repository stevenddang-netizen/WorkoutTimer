package com.steven.workouttimer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AudioType {
    BEEP,
    VOICE
}

@Entity(tableName = "timers")
data class TimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val totalMinutes: Int,
    val audioEnabled: Boolean = true,
    val audioType: String = AudioType.BEEP.name,
    val countdownSeconds: Int = 3,
    val createdAt: Long = System.currentTimeMillis()
)
