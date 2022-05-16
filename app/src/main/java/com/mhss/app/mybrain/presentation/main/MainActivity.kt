package com.mhss.app.mybrain.presentation.main

import android.os.Bundle
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
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.mhss.app.mybrain.presentation.tasks.TaskDetailScreen
import com.mhss.app.mybrain.presentation.tasks.TasksScreen
import com.mhss.app.mybrain.presentation.tasks.TasksSearchScreen
import com.mhss.app.mybrain.presentation.util.Screen
import com.mhss.app.mybrain.ui.theme.DarkBackground
import com.mhss.app.mybrain.ui.theme.MyBrainTheme
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.StartUpScreenSettings
import com.mhss.app.mybrain.util.settings.ThemeSettings
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
            val themMode = viewModel.themMode.collectAsState(initial = ThemeSettings.AUTO.value)
            var startUpScreenSettings by remember { mutableStateOf(StartUpScreenSettings.SPACES.value) }
            LaunchedEffect(true) {
                runBlocking {
                    startUpScreenSettings = viewModel.defaultStartUpScreen.first()
                }
            }
            val startUpScreen =
                if (startUpScreenSettings == StartUpScreenSettings.SPACES.value)
                    Screen.SpacesScreen.route else Screen.DashboardScreen.route
            val isDarkMode = when (themMode.value) {
                ThemeSettings.DARK.value -> true
                ThemeSettings.LIGHT.value -> false
                else -> isSystemInDarkTheme()
            }
            handleThemeChange(isDarkMode)
            MyBrainTheme(darkTheme = isDarkMode) {
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
                        composable(Screen.TasksScreen.route) {
                            TasksScreen(navController = navController)
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
                        composable(Screen.NotesScreen.route) {}
                        composable(Screen.NoteAddScreen.route) {}
                        composable(Screen.NoteDetailScreen.route) {}
                        composable(Screen.NoteSearchScreen.route) {}
                        composable(Screen.DiaryScreen.route) {}
                        composable(Screen.DiaryAddScreen.route) {}
                        composable(Screen.DiarySearchScreen.route) {}
                        composable(Screen.DiaryDetailScreen.route) {}
                        composable(Screen.DiarySummaryScreen.route) {}
                        composable(Screen.BookmarksScreen.route) {}
                        composable(Screen.BookmarkAddScreen.route) {}
                        composable(Screen.BookmarkDetailScreen.route) {}
                        composable(Screen.BookmarkSearchScreen.route) {}
                        composable(Screen.CalendarScreen.route) {}
                        composable(Screen.CalendarSearchScreen.route) {}
                    }
                }
            }
        }
    }

    private fun handleThemeChange(isDarkMode: Boolean) {
        window.statusBarColor = if (isDarkMode) DarkBackground.toArgb() else Color.White.toArgb()
        window.navigationBarColor =
            if (isDarkMode) DarkBackground.toArgb() else Color.White.toArgb()
    }
}