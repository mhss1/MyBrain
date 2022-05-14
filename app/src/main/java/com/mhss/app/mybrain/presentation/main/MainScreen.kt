package com.mhss.app.mybrain.presentation.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mhss.app.mybrain.presentation.util.BottomNavItem


@ExperimentalAnimationApi
@Composable
fun MainScreen(
    startUpScreen: String,
    mainNavController: NavHostController
) {
    val navController = rememberNavController()
    val bottomNavItems =
        listOf(BottomNavItem.Dashboard, BottomNavItem.Spaces, BottomNavItem.Settings)
    Scaffold(
        bottomBar = {
            MainBottomBar(navController = navController, items = bottomNavItems)
        }
    ) {
        NavigationGraph(
            navController = navController,
            mainNavController = mainNavController,
            startUpScreen = startUpScreen
        )
    }
}