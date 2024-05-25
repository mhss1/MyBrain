package com.mhss.app.mybrain.presentation.main.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MainBottomBar(
    navController: NavHostController,
    items: List<BottomNavItem>,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach {
            NavigationBarItem(
                icon = { Icon(
                    if (currentDestination?.route == it.screen::class.qualifiedName)
                        painterResource(it.iconSelected)
                    else
                        painterResource(it.icon),
                    contentDescription = stringResource(it.title),
                ) },
                selected = currentDestination?.route == it.screen::class.qualifiedName,
                onClick = {
                    navController.navigate(it.screen) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                alwaysShowLabel = false
            )
        }
    }
}