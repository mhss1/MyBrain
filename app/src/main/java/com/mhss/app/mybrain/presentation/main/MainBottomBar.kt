package com.mhss.app.mybrain.presentation.main

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
                    if (currentDestination?.route == it.route)
                            painterResource(it.iconSelected)
                        else
                            painterResource(it.icon)
                    ,
                    contentDescription = stringResource(it.title)
                ) },
                label = {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(it.title))
                },
                selected = currentDestination?.route == it.route,
                onClick = {
                    navController.navigate(it.route) {
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