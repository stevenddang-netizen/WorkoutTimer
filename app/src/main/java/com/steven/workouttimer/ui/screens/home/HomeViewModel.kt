package com.steven.workouttimer.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.steven.workouttimer.data.db.TimerEntity
import com.steven.workouttimer.data.repository.TimerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: TimerRepository
) : ViewModel() {

    val timers: StateFlow<List<TimerEntity>> = repository.allTimers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteTimer(timer: TimerEntity) {
        viewModelScope.launch {
            repository.deleteTimer(timer)
        }
    }

    companion object {
        fun factory(repository: TimerRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
        }
    }
}
