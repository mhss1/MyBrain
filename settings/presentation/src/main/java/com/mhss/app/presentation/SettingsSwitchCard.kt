package com.mhss.app.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSwitchCard(
    text: String,
    checked: Boolean,
    iconPainter: Painter? = null,
    onCheck: (Boolean) -> Unit = {}
) {
    SettingsItemCard(
        cornerRadius = 16.dp,
        onClick = {
            onCheck(!checked)
        },
        vPadding = 6.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            iconPainter?.let {
                Icon(
                    painter = it,
                    contentDescription = text,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Switch(checked = checked, onCheckedChange = {
            onCheck(it)
        })
    }
}