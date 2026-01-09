package com.steven.workouttimer.ui.navigation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.steven.workouttimer.WorkoutTimerApp
import com.steven.workouttimer.service.TimerService
import com.steven.workouttimer.service.TimerState
import com.steven.workouttimer.ui.screens.create.CreateTimerScreen
import com.steven.workouttimer.ui.screens.create.CreateTimerViewModel
import com.steven.workouttimer.ui.screens.fullscreen.FullScreenTimerScreen
import com.steven.workouttimer.ui.screens.home.HomeScreen
import com.steven.workouttimer.ui.screens.home.HomeViewModel
import com.steven.workouttimer.ui.screens.timer.TimerScreen
import com.steven.workouttimer.ui.screens.timer.TimerViewModel
import kotlinx.coroutines.flow.MutableStateFlow

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object CreateTimer : Screen("create_timer?timerId={timerId}") {
        fun createRoute(timerId: Long? = null) =
            if (timerId != null) "create_timer?timerId=$timerId" else "create_timer"
    }
    data object Timer : Screen("timer/{timerId}") {
        fun createRoute(timerId: Long) = "timer/$timerId"
    }
    data object FullScreenTimer : Screen("fullscreen_timer/{timerId}") {
        fun createRoute(timerId: Long) = "fullscreen_timer/$timerId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    val context = LocalContext.current
    val app = context.applicationContext as WorkoutTimerApp
    val repository = app.container.timerRepository
    val themePreferences = app.container.themePreferences
    val currentThemeMode by themePreferences.themeMode.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Global timer service binding for showing running timer on home screen
    var globalTimerService by remember { mutableStateOf<TimerService?>(null) }
    var globalBound by remember { mutableStateOf(false) }
    val runningTimerState by globalTimerService?.timerState?.collectAsState()
        ?: remember { mutableStateOf(TimerState()) }

    val globalConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as TimerService.TimerBinder
                globalTimerService = binder.getService()
                globalBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                globalTimerService = null
                globalBound = false
            }
        }
    }

    // Try to bind to existing service (if running)
    DisposableEffect(Unit) {
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, globalConnection, 0) // Don't auto-create

        onDispose {
            if (globalBound) {
                context.unbindService(globalConnection)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.factory(repository)
            )
            HomeScreen(
                viewModel = viewModel,
                currentThemeMode = currentThemeMode,
                onThemeModeChange = { themePreferences.setThemeMode(it) },
                runningTimerState = if (runningTimerState.isRunning) runningTimerState else null,
                onRunningTimerTap = {
                    navController.navigate(Screen.Timer.createRoute(runningTimerState.timerId))
                },
                onRunningTimerPlayPause = {
                    globalTimerService?.let { service ->
                        if (service.timerState.value.isPaused) {
                            service.resumeTimer()
                        } else {
                            service.pauseTimer()
                        }
                    }
                },
                onRunningTimerStop = {
                    globalTimerService?.stopTimer()
                },
                onRunningTimerDelete = {
                    val timerId = runningTimerState.timerId
                    globalTimerService?.stopTimer()
                    coroutineScope.launch {
                        repository.deleteTimerById(timerId)
                    }
                },
                onCreateTimer = {
                    navController.navigate(Screen.CreateTimer.createRoute())
                },
                onEditTimer = { timerId ->
                    navController.navigate(Screen.CreateTimer.createRoute(timerId))
                },
                onStartTimer = { timerId ->
                    navController.navigate(Screen.Timer.createRoute(timerId))
                }
            )
        }

        composable(
            route = Screen.CreateTimer.route,
            arguments = listOf(
                navArgument("timerId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val timerId = backStackEntry.arguments?.getLong("timerId")?.takeIf { it > 0 }
            val viewModel: CreateTimerViewModel = viewModel(
                factory = CreateTimerViewModel.factory(repository, timerId)
            )
            CreateTimerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                isEditing = timerId != null
            )
        }

        composable(
            route = Screen.Timer.route,
            arguments = listOf(
                navArgument("timerId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val timerId = backStackEntry.arguments?.getLong("timerId") ?: return@composable
            val viewModel: TimerViewModel = viewModel(
                factory = TimerViewModel.factory(context, repository, timerId)
            )
            TimerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                onFullScreen = {
                    navController.navigate(Screen.FullScreenTimer.createRoute(timerId))
                }
            )
        }

        composable(
            route = Screen.FullScreenTimer.route,
            arguments = listOf(
                navArgument("timerId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val timerId = backStackEntry.arguments?.getLong("timerId") ?: return@composable

            var timerService by remember { mutableStateOf<TimerService?>(null) }
            var bound by remember { mutableStateOf(false) }

            val connection = remember {
                object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        val binder = service as TimerService.TimerBinder
                        timerService = binder.getService()
                        bound = true
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        timerService = null
                        bound = false
                    }
                }
            }

            DisposableEffect(Unit) {
                val intent = Intent(context, TimerService::class.java)
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

                onDispose {
                    if (bound) {
                        context.unbindService(connection)
                    }
                }
            }

            timerService?.let { service ->
                FullScreenTimerScreen(
                    timerStateFlow = service.timerState,
                    onExitFullScreen = { navController.popBackStack() },
                    onPlayPause = {
                        if (service.timerState.value.isPaused) {
                            service.resumeTimer()
                        } else {
                            service.pauseTimer()
                        }
                    },
                    onStop = {
                        service.stopTimer()
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                )
            }
        }
    }
}
