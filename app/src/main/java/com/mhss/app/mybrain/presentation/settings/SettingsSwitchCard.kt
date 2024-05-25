package com.mhss.app.mybrain.presentation.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
            style = MaterialTheme.typography.titleLarge
        )
        Switch(checked = checked, onCheckedChange = {
            onCheck(it)
        })
    }
}