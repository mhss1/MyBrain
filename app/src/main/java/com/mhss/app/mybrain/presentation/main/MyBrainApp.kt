package com.mhss.app.mybrain.presentation.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.mhss.app.ui.R
import com.mhss.app.util.Constants
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.mybrain.presentation.app_lock.AuthScreen
import com.mhss.app.presentation.AssistantScreen
import com.mhss.app.presentation.BookmarkDetailsScreen
import com.mhss.app.presentation.BookmarkSearchScreen
import com.mhss.app.presentation.BookmarksScreen
import com.mhss.app.presentation.CalendarEventDetailsScreen
import com.mhss.app.presentation.CalendarScreen
import com.mhss.app.presentation.DiaryChartScreen
import com.mhss.app.presentation.DiaryEntryDetailsScreen
import com.mhss.app.presentation.DiaryScreen
import com.mhss.app.presentation.DiarySearchScreen
import com.mhss.app.presentation.ImportExportScreen
import com.mhss.app.presentation.integrations.IntegrationsScreen
import com.mhss.app.presentation.NoteDetailsScreen
import com.mhss.app.presentation.NoteFolderDetailsScreen
import com.mhss.app.presentation.NotesScreen
import com.mhss.app.presentation.NotesSearchScreen
import com.mhss.app.presentation.TaskDetailScreen
import com.mhss.app.presentation.TasksScreen
import com.mhss.app.presentation.TasksSearchScreen
import com.mhss.app.ui.StartUpScreenSettings
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.ui.theme.Rubik
import com.mhss.app.ui.toFontFamily
import com.mhss.app.ui.toInt
import org.koin.compose.KoinContext
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
    val context = LocalContext.current
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    var appUnlocked by remember {
        mutableStateOf(true)
    }
    val useMaterialYou by viewModel.useMaterialYou.collectAsStateWithLifecycle(false)
    val lifecycleOwner = LocalLifecycleOwner.current
    val font = viewModel.font.collectAsStateWithLifecycle(Rubik.toInt())
    var startDestination: Screen by remember { mutableStateOf(Screen.SpacesScreen) }
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (viewModel.lockApp.first()) {
                    appUnlocked = false
                    appLockManager.showAuthPrompt()
                }
                appLockManager.resultFlow.collectLatest { authResult ->
                    when (authResult) {
                        is AppLockManager.AuthResult.Error -> {
                            snackbarHostState.showSnackbar(
                                authResult.message
                            )
                        }

                        AppLockManager.AuthResult.Failed -> {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.auth_failed)
                            )
                        }

                        AppLockManager.AuthResult.NoHardware, AppLockManager.AuthResult.HardwareUnavailable -> {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.auth_no_hardware)
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
    KoinContext {
        MyBrainTheme(
            darkTheme = isDarkMode,
            useDynamicColors = useMaterialYou,
            fontFamily = font.value.toFontFamily()
        ) {
            val navController = rememberNavController()
            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                NavHost(
                    startDestination = Screen.Main,
                    navController = navController,
                ) {
                    composable<Screen.Main> {
                        MainScreen(
                            startUpScreen = startDestination,
                            mainNavController = navController,
                            appLockManager = appLockManager,
                            modifier = Modifier
                                .consumeWindowInsets(WindowInsets.systemBars)
                                .padding(paddingValues)
                        )
                    }
                    composable<Screen.TasksScreen>(
                        deepLinks =
                        listOf(
                            navDeepLink {
                                uriPattern =
                                    "${Constants.TASKS_SCREEN_URI}?${Constants.ADD_TASK_ARG}={${Constants.ADD_TASK_ARG}}"
                            }
                        ),
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
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
                        ),
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        val args = it.toRoute<Screen.TaskDetailScreen>()
                        TaskDetailScreen(
                            navController = navController,
                            args.taskId
                        )
                    }
                    composable<Screen.TaskSearchScreen>(
                        enterTransition = { slideUpTransition() },
                        exitTransition = { slideDownTransition() },
                        popEnterTransition = { slideUpTransition() },
                        popExitTransition = { slideDownTransition() }
                    ) {
                        TasksSearchScreen(navController = navController)
                    }
                    composable<Screen.NotesScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        NotesScreen(navController = navController)
                    }
                    composable<Screen.NoteDetailsScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        val args = it.toRoute<Screen.NoteDetailsScreen>()
                        NoteDetailsScreen(
                            navController,
                            args.noteId,
                            args.folderId
                        )
                    }
                    composable<Screen.NoteSearchScreen>(
                        enterTransition = { slideUpTransition() },
                        exitTransition = { slideDownTransition() },
                        popEnterTransition = { slideUpTransition() },
                        popExitTransition = { slideDownTransition() }
                    ) {
                        NotesSearchScreen(navController = navController)
                    }
                    composable<Screen.DiaryScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        DiaryScreen(navController = navController)
                    }
                    composable<Screen.DiaryChartScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        DiaryChartScreen()
                    }
                    composable<Screen.DiarySearchScreen>(
                        enterTransition = { slideUpTransition() },
                        exitTransition = { slideDownTransition() },
                        popEnterTransition = { slideUpTransition() },
                        popExitTransition = { slideDownTransition() }
                    ) {
                        DiarySearchScreen(navController = navController)
                    }
                    composable<Screen.DiaryDetailScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        val args = it.toRoute<Screen.DiaryDetailScreen>()
                        DiaryEntryDetailsScreen(
                            navController = navController,
                            args.entryId
                        )
                    }
                    composable<Screen.BookmarksScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        BookmarksScreen(navController = navController)
                    }
                    composable<Screen.BookmarkDetailScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        val args = it.toRoute<Screen.BookmarkDetailScreen>()
                        BookmarkDetailsScreen(
                            navController = navController,
                            args.bookmarkId
                        )
                    }
                    composable<Screen.BookmarkSearchScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        BookmarkSearchScreen(navController = navController)
                    }
                    composable<Screen.CalendarScreen>(
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern = Constants.CALENDAR_SCREEN_URI
                            }
                        ),
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        CalendarScreen(navController = navController)
                    }
                    composable<Screen.CalendarEventDetailsScreen>(
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern =
                                    "${Constants.CALENDAR_DETAILS_SCREEN_URI}?${Constants.CALENDAR_EVENT_ARG}={${Constants.CALENDAR_EVENT_ARG}}"
                            }
                        ),
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        val args = it.toRoute<Screen.CalendarEventDetailsScreen>()
                        CalendarEventDetailsScreen(
                            navController = navController,
                            eventJson = args.eventJson
                        )
                    }
                    composable<Screen.NoteFolderDetailsScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        val args = it.toRoute<Screen.NoteFolderDetailsScreen>()
                        NoteFolderDetailsScreen(
                            navController = navController,
                            args.folderId
                        )
                    }
                    composable<Screen.ImportExportScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        ImportExportScreen()
                    }
                    composable<Screen.IntegrationsScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        IntegrationsScreen()
                    }
                    composable<Screen.AssistantScreen>(
                        enterTransition = { slideInTransition() },
                        exitTransition = { slideOutTransition() },
                        popEnterTransition = { slideInTransition() },
                        popExitTransition = { slideOutTransition() }
                    ) {
                        AssistantScreen()
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
}

fun slideInTransition() = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(300)
)

fun slideOutTransition() = slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(300)
)

fun slideUpTransition() = slideInVertically(
    initialOffsetY = { it },
    animationSpec = tween(300)
)

fun slideDownTransition() = slideOutVertically(
    targetOffsetY = { it },
    animationSpec = tween(300)
)