package com.mhss.app.mybrain

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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
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
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.mybrain.presentation.app_lock.AuthResult
import com.mhss.app.mybrain.presentation.app_lock.AuthScreen
import com.mhss.app.mybrain.presentation.main.MainScreen
import com.mhss.app.mybrain.presentation.main.MainViewModel
import com.mhss.app.mybrain.presentation.localsync.LocalSyncScreen
import com.mhss.app.mybrain.sync.util.SYNC_DEEP_LINK_PATTERN
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
import com.mhss.app.presentation.NoteDetailsScreen
import com.mhss.app.presentation.NoteFolderDetailsScreen
import com.mhss.app.presentation.NotesScreen
import com.mhss.app.presentation.NotesSearchScreen
import com.mhss.app.presentation.TaskDetailScreen
import com.mhss.app.presentation.TasksScreen
import com.mhss.app.presentation.TasksSearchScreen
import com.mhss.app.presentation.backup.ImportExportScreen
import com.mhss.app.presentation.integrations.IntegrationsScreen
import com.mhss.app.ui.AppFont
import com.mhss.app.ui.Res
import com.mhss.app.ui.StartUpScreenSettings
import com.mhss.app.ui.auth_failed
import com.mhss.app.ui.auth_no_hardware
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import com.mhss.app.ui.snackbar.showSnackbar
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.ui.theme.toFontFamily
import com.mhss.app.ui.toAppFont
import com.mhss.app.ui.toFontSizeScale
import com.mhss.app.ui.toStartUpScreen
import com.mhss.app.util.Constants
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
    val snackbarHostState = remember { SnackbarHostState() }
    var appUnlocked by remember { mutableStateOf(true) }
    val useMaterialYou by viewModel.useMaterialYou.collectAsStateWithLifecycle(false)
    val lifecycleOwner = LocalLifecycleOwner.current
    val font = viewModel.font.collectAsStateWithLifecycle(AppFont.IBM_PLEX.value)
    val fontSize = viewModel.fontSize.collectAsStateWithLifecycle(1)
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
                        is AuthResult.Error -> {
                            snackbarHostState.showSnackbar(
                                authResult.message
                            )
                        }

                        AuthResult.Failed -> {
                            snackbarHostState.showSnackbar(Res.string.auth_failed)
                        }

                        AuthResult.NoHardware, AuthResult.HardwareUnavailable -> {
                            snackbarHostState.showSnackbar(Res.string.auth_no_hardware)
                        }

                        AuthResult.Success -> {
                            appUnlocked = true
                        }

                        AuthResult.NoneEnrolled -> {
                            viewModel.disableAppLock()
                            appUnlocked = true
                        }
                    }
                }
            }
        }
    }
    MyBrainTheme(
        darkTheme = isDarkMode,
        useDynamicColors = useMaterialYou,
        fontFamily = font.value.toAppFont().toFontFamily(),
        fontSizeScale = fontSize.value.toFontSizeScale()
    ) {
        val navController = rememberNavController()
        LaunchedEffect(Unit) {
            when (val defaultScreen = viewModel.defaultStartUpScreen.first()) {
                StartUpScreenSettings.DASHBOARD.value -> startDestination = Screen.DashboardScreen
                StartUpScreenSettings.SPACES.value -> Unit
                else -> navController.navigate(defaultScreen.toStartUpScreen().screen)
            }
        }
        Scaffold(
            modifier = modifier.fillMaxSize().semantics { testTagsAsResourceId = true },
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { LocalisedSnackbarHost(snackbarHostState) }
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
                                uriPattern = Constants.TASKS_SCREEN_URI
                            }
                        ),
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
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
                ) {
                    TasksSearchScreen(navController = navController)
                }
                composable<Screen.NotesScreen>(
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
                ) {
                    NotesScreen(navController = navController)
                }
                composable<Screen.NoteDetailsScreen>(
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern = "${Constants.NOTE_DETAILS_URI}/{${Constants.NOTE_ID_ARG}}"
                        }
                    ),
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
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
                ) {
                    NotesSearchScreen(navController = navController)
                }
                composable<Screen.DiaryScreen>(
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
                ) {
                    DiaryScreen(navController = navController)
                }
                composable<Screen.DiaryChartScreen>(
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
                ) {
                    DiaryChartScreen()
                }
                composable<Screen.DiarySearchScreen>(
                    enterTransition = { slideUpTransition() },
                    exitTransition = { slideDownTransition() },
                ) {
                    DiarySearchScreen(navController = navController)
                }
                composable<Screen.DiaryDetailScreen>(
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
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
                ) {
                    BookmarksScreen(navController = navController)
                }
                composable<Screen.BookmarkDetailScreen>(
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
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
                ) {
                    CalendarScreen(navController = navController)
                }
                composable<Screen.CalendarEventDetailsScreen>(
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern =
                                "${Constants.CALENDAR_DETAILS_SCREEN_URI}?${Constants.CALENDAR_EVENT_ID_ARG}={${Constants.CALENDAR_EVENT_ID_ARG}}"
                        }
                    ),
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
                ) {
                    val args = it.toRoute<Screen.CalendarEventDetailsScreen>()
                    CalendarEventDetailsScreen(
                        navController = navController,
                        eventId = args.eventId,
                        initialStartMillis = args.initialStartMillis
                    )
                }
                composable<Screen.NoteFolderDetailsScreen>(
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
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
                ) {
                    ImportExportScreen()
                }
                composable<Screen.IntegrationsScreen>(
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
                ) {
                    IntegrationsScreen()
                }
                composable<Screen.LocalSyncScreen>(
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern = SYNC_DEEP_LINK_PATTERN
                        }
                    ),
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
                ) {
                    val args = it.toRoute<Screen.LocalSyncScreen>()
                    LocalSyncScreen(navController = navController, pairArgs = args)
                }
                composable<Screen.AssistantScreen>(
                    enterTransition = { slideInTransition() },
                    exitTransition = { slideOutTransition() },
                ) {
                    AssistantScreen(navController = navController)
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
