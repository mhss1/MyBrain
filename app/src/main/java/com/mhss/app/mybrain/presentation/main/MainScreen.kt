package com.mhss.app.mybrain.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.mybrain.presentation.main.components.MainBottomBar
import com.mhss.app.mybrain.presentation.main.components.NavigationGraph
import com.mhss.app.mybrain.presentation.main.components.BottomNavItem
import com.mhss.app.mybrain.presentation.common.navigation.Screen

@Composable
fun MainScreen(
    startUpScreen: Screen,
    mainNavController: NavHostController,
    appLockManager: AppLockManager
) {
    val navController = rememberNavController()
    val bottomNavItems =
        listOf(BottomNavItem.Dashboard, BottomNavItem.Spaces, BottomNavItem.Settings)
    Scaffold(
        bottomBar = {
            MainBottomBar(navController = navController, items = bottomNavItems)
        }
    ) {  paddingValues ->
        NavigationGraph(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            mainNavController = mainNavController,
            startUpScreen = startUpScreen,
            appLockManager
        )
    }
}