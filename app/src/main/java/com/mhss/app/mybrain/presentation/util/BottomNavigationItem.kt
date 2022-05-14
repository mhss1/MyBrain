package com.mhss.app.mybrain.presentation.util

import com.mhss.app.mybrain.R

sealed class BottomNavItem(val title: Int, val icon: Int, val iconSelected: Int, val route: String){

    object Dashboard : BottomNavItem(R.string.dashboard, R.drawable.ic_home, R.drawable.ic_home_filled, Screen.DashboardScreen.route)
    object Spaces : BottomNavItem(R.string.spaces, R.drawable.ic_spaces, R.drawable.ic_spaces_filled, Screen.SpacesScreen.route)
    object Settings: BottomNavItem(R.string.settings, R.drawable.ic_settings, R.drawable.ic_settings_filled, Screen.SettingsScreen.route)

}