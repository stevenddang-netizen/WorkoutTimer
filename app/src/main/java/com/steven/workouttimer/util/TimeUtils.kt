package com.steven.workouttimer.util

object TimeUtils {
    fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun formatTimeWithHours(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun formatMinutes(minutes: Int): String {
        return if (minutes >= 60) {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            val hourLabel = if (hours == 1) "hour" else "hours"
            if (remainingMinutes > 0) {
                val minLabel = if (remainingMinutes == 1) "minute" else "minutes"
                "$hours $hourLabel $remainingMinutes $minLabel"
            } else {
                "$hours $hourLabel"
            }
        } else {
            val minLabel = if (minutes == 1) "minute" else "minutes"
            "$minutes $minLabel"
        }
    }
}
