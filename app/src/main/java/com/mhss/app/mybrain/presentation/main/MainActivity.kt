package com.mhss.app.mybrain.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mhss.app.mybrain.domain.use_case.notes.NoteFolderDetailsScreen
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
import com.mhss.app.mybrain.presentation.util.Screen
import com.mhss.app.mybrain.ui.theme.MyBrainTheme
import com.mhss.app.mybrain.ui.theme.Rubik
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.StartUpScreenSettings
import com.mhss.app.mybrain.util.settings.ThemeSettings
import com.mhss.app.mybrain.util.settings.toFontFamily
import com.mhss.app.mybrain.util.settings.toInt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Suppress("BlockingMethodInNonBlockingContext")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode = viewModel.themeMode.collectAsState(initial = ThemeSettings.AUTO.value)
            val font = viewModel.font.collectAsState(initial = Rubik.toInt())
            val blockScreenshots = viewModel.blockScreenshots.collectAsState(initial = false)
            var startUpScreenSettings by remember { mutableStateOf(StartUpScreenSettings.SPACES.value) }
            val systemUiController = rememberSystemUiController()
            LaunchedEffect(true) {
                runBlocking {
                    startUpScreenSettings = viewModel.defaultStartUpScreen.first()
                }
                if (!isNotificationPermissionGranted())
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        0
                    )
            }
            LaunchedEffect(blockScreenshots.value) {
                if (blockScreenshots.value) {
                    window.setFlags(
                        LayoutParams.FLAG_SECURE,
                        LayoutParams.FLAG_SECURE
                    )
                } else
                    window.clearFlags(LayoutParams.FLAG_SECURE)
            }
            val startUpScreen =
                if (startUpScreenSettings == StartUpScreenSettings.SPACES.value)
                    Screen.SpacesScreen.route else Screen.DashboardScreen.route
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
                        startDestination = Screen.Main.route,
                        navController = navController
                    ) {
                        composable(Screen.Main.route) {
                            MainScreen(
                                startUpScreen = startUpScreen,
                                mainNavController = navController
                            )
                        }
                        composable(
                            Screen.TasksScreen.route,
                            arguments = listOf(navArgument(Constants.ADD_TASK_ARG) {
                                type = NavType.BoolType
                                defaultValue = false
                            }),
                            deepLinks =
                            listOf(
                                navDeepLink {
                                    uriPattern =
                                        "${Constants.TASKS_SCREEN_URI}/{${Constants.ADD_TASK_ARG}}"
                                }
                            )
                        ) {
                            TasksScreen(
                                navController = navController,
                                addTask = it.arguments?.getBoolean(Constants.ADD_TASK_ARG)
                                    ?: false
                            )
                        }
                        composable(
                            Screen.TaskDetailScreen.route,
                            arguments = listOf(navArgument(Constants.TASK_ID_ARG) {
                                type = NavType.IntType
                            }),
                            deepLinks =
                            listOf(
                                navDeepLink {
                                    uriPattern =
                                        "${Constants.TASK_DETAILS_URI}/{${Constants.TASK_ID_ARG}}"
                                }
                            )
                        ) {
                            TaskDetailScreen(
                                navController = navController,
                                it.arguments?.getInt(Constants.TASK_ID_ARG)!!
                            )
                        }
                        composable(Screen.TaskSearchScreen.route) {
                            TasksSearchScreen(navController = navController)
                        }
                        composable(
                            Screen.NotesScreen.route
                        ) {
                            NotesScreen(navController = navController)
                        }
                        composable(
                            Screen.NoteDetailsScreen.route,
                            arguments = listOf(navArgument(Constants.NOTE_ID_ARG) {
                                type = NavType.IntType
                            },
                                navArgument(Constants.FOLDER_ID) {
                                    type = NavType.StringType
                                }
                            ),
                        ) {
                            NoteDetailsScreen(
                                navController,
                                it.arguments?.getInt(Constants.NOTE_ID_ARG) ?: -1,
                                it.arguments?.getString(Constants.FOLDER_ID) ?: ""
                            )
                        }
                        composable(Screen.NoteSearchScreen.route) {
                            NotesSearchScreen(navController = navController)
                        }
                        composable(Screen.DiaryScreen.route) {
                            DiaryScreen(navController = navController)
                        }
                        composable(Screen.DiaryChartScreen.route) {
                            DiaryChartScreen()
                        }
                        composable(Screen.DiarySearchScreen.route) {
                            DiarySearchScreen(navController = navController)
                        }
                        composable(
                            Screen.DiaryDetailScreen.route,
                            arguments = listOf(navArgument(Constants.DIARY_ID_ARG) {
                                type = NavType.IntType
                            })
                        ) {
                            DiaryEntryDetailsScreen(
                                navController = navController,
                                it.arguments?.getInt(Constants.DIARY_ID_ARG)!!
                            )
                        }
                        composable(Screen.BookmarksScreen.route) {
                            BookmarksScreen(navController = navController)
                        }
                        composable(
                            Screen.BookmarkDetailScreen.route,
                            arguments = listOf(navArgument(Constants.BOOKMARK_ID_ARG) {
                                type = NavType.IntType
                            })
                        ) {
                            BookmarkDetailsScreen(
                                navController = navController,
                                it.arguments?.getInt(Constants.BOOKMARK_ID_ARG)!!
                            )
                        }
                        composable(Screen.BookmarkSearchScreen.route) {
                            BookmarkSearchScreen(navController = navController)
                        }
                        composable(
                            Screen.CalendarScreen.route,
                            deepLinks = listOf(
                                navDeepLink {
                                    uriPattern = Constants.CALENDAR_SCREEN_URI
                                }
                            )
                        ) {
                            CalendarScreen(navController = navController)
                        }
                        composable(
                            Screen.CalendarEventDetailsScreen.route,
                            arguments = listOf(navArgument(Constants.CALENDAR_EVENT_ARG) {
                                type = NavType.StringType
                            }),
                            deepLinks = listOf(
                                navDeepLink {
                                    uriPattern =
                                        "${Constants.CALENDAR_DETAILS_SCREEN_URI}/{${Constants.CALENDAR_EVENT_ARG}}"
                                }
                            )
                        ) {
                            CalendarEventDetailsScreen(
                                navController = navController,
                                eventJson = it.arguments?.getString(Constants.CALENDAR_EVENT_ARG)
                                    ?: ""
                            )
                        }
                        composable(
                            Screen.NoteFolderDetailsScreen.route,
                            arguments = listOf(navArgument(Constants.FOLDER_ID) {
                                type = NavType.StringType
                            })
                        ) {
                            NoteFolderDetailsScreen(
                                navController = navController,
                                it.arguments?.getString(Constants.FOLDER_ID) ?: ""
                            )
                        }
                        composable(Screen.ImportExportScreen.route) {
                            ImportExportScreen()
                        }
                    }
                }
            }
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    }
}