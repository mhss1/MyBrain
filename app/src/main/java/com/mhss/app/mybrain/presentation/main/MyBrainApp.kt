package com.mhss.app.mybrain.presentation.main

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.getString
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.mybrain.presentation.app_lock.AuthScreen
import com.mhss.app.mybrain.presentation.bookmarks.BookmarkDetailsScreen
import com.mhss.app.mybrain.presentation.bookmarks.BookmarkSearchScreen
import com.mhss.app.mybrain.presentation.bookmarks.BookmarksScreen
import com.mhss.app.mybrain.presentation.calendar.CalendarEventDetailsScreen
import com.mhss.app.mybrain.presentation.calendar.CalendarScreen
import com.mhss.app.mybrain.presentation.diary.DiaryChartScreen
import com.mhss.app.mybrain.presentation.diary.DiaryEntryDetailsScreen
import com.mhss.app.mybrain.presentation.diary.DiaryScreen
import com.mhss.app.mybrain.presentation.diary.DiarySearchScreen
import com.mhss.app.mybrain.presentation.navigation.Screen
import com.mhss.app.mybrain.presentation.notes.NoteDetailsScreen
import com.mhss.app.mybrain.presentation.notes.NoteFolderDetailsScreen
import com.mhss.app.mybrain.presentation.notes.NotesScreen
import com.mhss.app.mybrain.presentation.notes.NotesSearchScreen
import com.mhss.app.mybrain.presentation.settings.ImportExportScreen
import com.mhss.app.mybrain.presentation.tasks.TaskDetailScreen
import com.mhss.app.mybrain.presentation.tasks.TasksScreen
import com.mhss.app.mybrain.presentation.tasks.TasksSearchScreen
import com.mhss.app.mybrain.ui.theme.MyBrainTheme
import com.mhss.app.mybrain.ui.theme.Rubik
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.StartUpScreenSettings
import com.mhss.app.mybrain.util.settings.toFontFamily
import com.mhss.app.mybrain.util.settings.toInt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MyBrainApp(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    isDarkMode: Boolean,
    appLockManager: AppLockManager
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    var appUnlocked by remember {
        mutableStateOf(true)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val font = viewModel.font.collectAsState(initial = Rubik.toInt())
    var startDestination: Screen by remember { mutableStateOf(Screen.SpacesScreen) }
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (viewModel.lockApp.first()) {
                    appUnlocked = false
                    appLockManager.showAuthPrompt()
                }
                appLockManager.resultFlow.collectLatest { authResult ->
                    println(authResult)
                    when (authResult) {
                        is AppLockManager.AuthResult.Error -> {
                            snackbarHostState.showSnackbar(
                                authResult.message
                            )
                        }

                        AppLockManager.AuthResult.Failed -> {
                            snackbarHostState.showSnackbar(
                                getString(R.string.auth_failed)
                            )
                        }

                        AppLockManager.AuthResult.NoHardware, AppLockManager.AuthResult.HardwareUnavailable -> {
                            snackbarHostState.showSnackbar(
                                getString(R.string.auth_no_hardware)
                            )
                        }

                        AppLockManager.AuthResult.Success -> {
                            appUnlocked = true
                        }

                        AppLockManager.AuthResult.NoneEnrolled -> {
                            // User disabled biometric authentication
                            viewModel.disableAppLock()
                            appUnlocked = true
                        }
                    }
                }
            }
        }
        if (viewModel.defaultStartUpScreen.first() == StartUpScreenSettings.DASHBOARD.value) {
            startDestination = Screen.DashboardScreen
        }
    }
    MyBrainTheme(darkTheme = isDarkMode, fontFamily = font.value.toFontFamily()) {
        val navController = rememberNavController()
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            NavHost(
                startDestination = Screen.Main,
                navController = navController,
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding()
                ).consumeWindowInsets(paddingValues)
            ) {
                composable<Screen.Main> {
                    MainScreen(
                        startUpScreen = startDestination,
                        mainNavController = navController,
                        appLockManager = appLockManager
                    )
                }
                composable<Screen.TasksScreen>(
                    deepLinks =
                    listOf(
                        navDeepLink {
                            uriPattern =
                                "${Constants.TASKS_SCREEN_URI}?${Constants.ADD_TASK_ARG}={${Constants.ADD_TASK_ARG}}"
                        }
                    )
                ) {
                    val args = it.toRoute<Screen.TasksScreen>()
                    TasksScreen(
                        navController = navController,
                        addTask = args.addTask
                    )
                }
                composable<Screen.TaskDetailScreen>(
                    deepLinks =
                    listOf(
                        navDeepLink {
                            uriPattern =
                                "${Constants.TASK_DETAILS_URI}/{${Constants.TASK_ID_ARG}}"
                        }
                    )
                ) {
                    val args = it.toRoute<Screen.TaskDetailScreen>()
                    TaskDetailScreen(
                        navController = navController,
                        args.taskId
                    )
                }
                composable<Screen.TaskSearchScreen> {
                    TasksSearchScreen(navController = navController)
                }
                composable<Screen.NotesScreen> {
                    NotesScreen(navController = navController)
                }
                composable<Screen.NoteDetailsScreen> {
                    val args = it.toRoute<Screen.NoteDetailsScreen>()
                    NoteDetailsScreen(
                        navController,
                        args.noteId,
                        args.folderId
                    )
                }
                composable<Screen.NoteSearchScreen> {
                    NotesSearchScreen(navController = navController)
                }
                composable<Screen.DiaryScreen> {
                    DiaryScreen(navController = navController)
                }
                composable<Screen.DiaryChartScreen> {
                    DiaryChartScreen()
                }
                composable<Screen.DiarySearchScreen> {
                    DiarySearchScreen(navController = navController)
                }
                composable<Screen.DiaryDetailScreen> {
                    val args = it.toRoute<Screen.DiaryDetailScreen>()
                    DiaryEntryDetailsScreen(
                        navController = navController,
                        args.entryId
                    )
                }
                composable<Screen.BookmarksScreen> {
                    BookmarksScreen(navController = navController)
                }
                composable<Screen.BookmarkDetailScreen> {
                    val args = it.toRoute<Screen.BookmarkDetailScreen>()
                    BookmarkDetailsScreen(
                        navController = navController,
                        args.bookmarkId
                    )
                }
                composable<Screen.BookmarkSearchScreen> {
                    BookmarkSearchScreen(navController = navController)
                }
                composable<Screen.CalendarScreen>(
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern = Constants.CALENDAR_SCREEN_URI
                        }
                    )
                ) {
                    CalendarScreen(navController = navController)
                }
                composable<Screen.CalendarEventDetailsScreen>(
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern =
                                "${Constants.CALENDAR_DETAILS_SCREEN_URI}?${Constants.CALENDAR_EVENT_ARG}={${Constants.CALENDAR_EVENT_ARG}}"
                        }
                    )
                ) {
                    val args = it.toRoute<Screen.CalendarEventDetailsScreen>()
                    CalendarEventDetailsScreen(
                        navController = navController,
                        eventJson = args.eventJson
                    )
                }
                composable<Screen.NoteFolderDetailsScreen> {
                    val args = it.toRoute<Screen.NoteFolderDetailsScreen>()
                    NoteFolderDetailsScreen(
                        navController = navController,
                        args.folderId
                    )
                }
                composable<Screen.ImportExportScreen> {
                    ImportExportScreen()
                }
            }
            if (!appUnlocked) {
                AuthScreen {
                    appLockManager.showAuthPrompt()
                }
            }
        }
    }
}