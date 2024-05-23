package com.mhss.app.mybrain.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.presentation.notes.NoteFolderDetailsScreen
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
import com.mhss.app.mybrain.presentation.notes.NoteDetailsScreen
import com.mhss.app.mybrain.presentation.notes.NotesScreen
import com.mhss.app.mybrain.presentation.notes.NotesSearchScreen
import com.mhss.app.mybrain.presentation.settings.ImportExportScreen
import com.mhss.app.mybrain.presentation.tasks.TaskDetailScreen
import com.mhss.app.mybrain.presentation.tasks.TasksScreen
import com.mhss.app.mybrain.presentation.tasks.TasksSearchScreen
import com.mhss.app.mybrain.presentation.navigation.Screen
import com.mhss.app.mybrain.ui.theme.MyBrainTheme
import com.mhss.app.mybrain.ui.theme.Rubik
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.StartUpScreenSettings
import com.mhss.app.mybrain.util.settings.ThemeSettings
import com.mhss.app.mybrain.util.settings.toFontFamily
import com.mhss.app.mybrain.util.settings.toInt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private val appLockManager by lazy {
        AppLockManager(this)
    }
    private var appUnlocked by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode = viewModel.themeMode.collectAsState(initial = ThemeSettings.AUTO.value)
            val font = viewModel.font.collectAsState(initial = Rubik.toInt())
            val blockScreenshots by viewModel.blockScreenshots.collectAsState(initial = false)
            val systemUiController = rememberSystemUiController()
            var startDestination: Screen by remember { mutableStateOf(Screen.SpacesScreen) }

            LaunchedEffect(Unit) {
                if (viewModel.defaultStartUpScreen.first() == StartUpScreenSettings.DASHBOARD.value) {
                    startDestination = Screen.DashboardScreen
                }
                if (!isNotificationPermissionGranted())
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        0
                    )
            }

            LaunchedEffect(blockScreenshots) {
                if (blockScreenshots) {
                    window.setFlags(
                        LayoutParams.FLAG_SECURE,
                        LayoutParams.FLAG_SECURE
                    )
                } else
                    window.clearFlags(LayoutParams.FLAG_SECURE)
            }
            val isDarkMode = when (themeMode.value) {
                ThemeSettings.DARK.value -> true
                ThemeSettings.LIGHT.value -> false
                else -> isSystemInDarkTheme()
            }
            SideEffect {
                systemUiController.setSystemBarsColor(
                    if (isDarkMode) Color.Black else Color.White,
                    darkIcons = !isDarkMode
                )
            }
            MyBrainTheme(darkTheme = isDarkMode, fontFamily = font.value.toFontFamily()) {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    NavHost(
                        startDestination = Screen.Main,
                        navController = navController
                    ) {
                        composable<Screen.Main> {
                            MainScreen(
                                startUpScreen = startDestination,
                                mainNavController = navController
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
                            println(it.arguments.toString())
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (viewModel.lockApp.first()) {
                    appUnlocked = false
                }
                appLockManager.resultFlow.collectLatest { authResult ->
                    when (authResult) {
                        is AppLockManager.AuthResult.Error -> {
                            toast(authResult.message)
                        }

                        AppLockManager.AuthResult.Failed -> {
                            toast(
                                this@MainActivity.getString(R.string.auth_failed)
                            )
                        }

                        AppLockManager.AuthResult.NoHardware, AppLockManager.AuthResult.HardwareUnavailable -> {
                            toast(
                                this@MainActivity.getString(R.string.auth_no_hardware)
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
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !appUnlocked) {
            appLockManager.showAuthPrompt()
        }
    }
}