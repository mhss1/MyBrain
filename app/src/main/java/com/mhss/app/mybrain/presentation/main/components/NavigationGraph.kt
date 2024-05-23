package com.mhss.app.mybrain.presentation.main.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mhss.app.mybrain.presentation.main.DashboardScreen
import com.mhss.app.mybrain.presentation.main.SettingsScreen
import com.mhss.app.mybrain.presentation.main.SpacesScreen
import com.mhss.app.mybrain.presentation.navigation.Screen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    mainNavController: NavHostController,
    startUpScreen: Screen
) {
    NavHost(navController = navController, startDestination = startUpScreen){

        composable<Screen.DashboardScreen> {
            DashboardScreen(mainNavController)
        }
        composable<Screen.SpacesScreen> {
            SpacesScreen(mainNavController)
        }
        composable<Screen.SettingsScreen> {
            SettingsScreen(mainNavController)
        }
    }
}