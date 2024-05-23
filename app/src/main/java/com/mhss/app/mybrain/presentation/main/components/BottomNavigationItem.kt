package com.mhss.app.mybrain.presentation.main.components

import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.presentation.navigation.Screen

sealed class BottomNavItem(val title: Int, val icon: Int, val iconSelected: Int, val screen: Screen){

    data object Dashboard : BottomNavItem(R.string.dashboard, R.drawable.ic_home, R.drawable.ic_home_filled,
        Screen.DashboardScreen
    )
    data object Spaces : BottomNavItem(R.string.spaces, R.drawable.ic_spaces, R.drawable.ic_spaces_filled,
        Screen.SpacesScreen
    )
    data object Settings: BottomNavItem(R.string.settings, R.drawable.ic_settings, R.drawable.ic_settings_filled,
        Screen.SettingsScreen
    )

}