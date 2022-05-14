package com.mhss.app.mybrain.presentation.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mhss.app.mybrain.presentation.util.Screen

@ExperimentalAnimationApi
@Composable
fun NavigationGraph(
    navController: NavHostController,
    mainNavController: NavHostController,
    startUpScreen: String
) {
    NavHost(navController = navController, startDestination = startUpScreen){

        composable(Screen.DashboardScreen.route){
            DashboardScreen(mainNavController)
        }
        composable(Screen.SpacesScreen.route){
            SpacesScreen(mainNavController)
        }
        composable(Screen.SettingsScreen.route){
            SettingsScreen()
        }
    }
}