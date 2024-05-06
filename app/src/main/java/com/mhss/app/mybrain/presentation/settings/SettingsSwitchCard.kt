package com.mhss.app.mybrain.presentation.settings

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSwitchCard(
    text: String,
    checked: Boolean,
    onCheck: (Boolean) -> Unit = {}
) {
    SettingsItemCard(
        cornerRadius = 16.dp,
        onClick = {
            onCheck(!checked)
        },
        vPadding = 10.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.h6
        )
        Switch(checked = checked, onCheckedChange = {
            onCheck(it)
        })
    }
}