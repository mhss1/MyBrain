package com.mhss.app.mybrain.presentation.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun NumberPicker(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = label, style = MaterialTheme.typography.body1)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onValueChange(value - 1) }) {
                Text(text = "-", style = MaterialTheme.typography.body1)
            }
            Text(text = value.toString(), style = MaterialTheme.typography.body1)
            TextButton(onClick = { onValueChange(value + 1) }) {
                Text(text = "+", style = MaterialTheme.typography.body1)
            }
        }
    }
}