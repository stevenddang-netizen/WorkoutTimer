package com.steven.workouttimer.data.repository

import com.steven.workouttimer.data.db.TimerDao
import com.steven.workouttimer.data.db.TimerEntity
import kotlinx.coroutines.flow.Flow

class TimerRepository(private val timerDao: TimerDao) {

    val allTimers: Flow<List<TimerEntity>> = timerDao.getAllTimers()

    suspend fun getTimerById(id: Long): TimerEntity? {
        return timerDao.getTimerById(id)
    }

    suspend fun insertTimer(timer: TimerEntity): Long {
        return timerDao.insertTimer(timer)
    }

    suspend fun updateTimer(timer: TimerEntity) {
        timerDao.updateTimer(timer)
    }

    suspend fun deleteTimer(timer: TimerEntity) {
        timerDao.deleteTimer(timer)
    }

    suspend fun deleteTimerById(id: Long) {
        timerDao.deleteTimerById(id)
    }
}
