package com.mhss.app.mybrain.presentation.main.components

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mhss.app.mybrain.presentation.util.BottomNavItem

@Composable
fun MainBottomBar(
    navController: NavHostController,
    items: List<BottomNavItem>,
) {
    BottomNavigation (backgroundColor = MaterialTheme.colors.background) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach {
            BottomNavigationItem(
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