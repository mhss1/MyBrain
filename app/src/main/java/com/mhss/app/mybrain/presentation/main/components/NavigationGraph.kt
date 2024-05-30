package com.mhss.app.mybrain.presentation.main.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.mybrain.presentation.main.DashboardScreen
import com.mhss.app.mybrain.presentation.main.SettingsScreen
import com.mhss.app.mybrain.presentation.main.SpacesScreen
import com.mhss.app.mybrain.presentation.navigation.Screen

@Composable
fun NavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mainNavController: NavHostController,
    startUpScreen: Screen,
    appLockManager: AppLockManager
) {
    NavHost(modifier = modifier, navController = navController, startDestination = startUpScreen){

        composable<Screen.DashboardScreen> {
            DashboardScreen(mainNavController)
        }
        composable<Screen.SpacesScreen> {
            SpacesScreen(mainNavController)
        }
        composable<Screen.SettingsScreen> {
            SettingsScreen(mainNavController, appLockManager)
        }
    }
}