package com.mhss.app.mybrain.presentation.main

import android.annotation.SuppressLint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mhss.app.mybrain.presentation.main.components.MainBottomBar
import com.mhss.app.mybrain.presentation.main.components.NavigationGraph
import com.mhss.app.mybrain.presentation.main.components.BottomNavItem
import com.mhss.app.mybrain.presentation.navigation.Screen

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(
    startUpScreen: Screen,
    mainNavController: NavHostController
) {
    val navController = rememberNavController()
    val bottomNavItems =
        listOf(BottomNavItem.Dashboard, BottomNavItem.Spaces, BottomNavItem.Settings)
    Scaffold(
        bottomBar = {
            MainBottomBar(navController = navController, items = bottomNavItems)
        }
    ) {  _ ->
        NavigationGraph(
            navController = navController,
            mainNavController = mainNavController,
            startUpScreen = startUpScreen
        )
    }
}