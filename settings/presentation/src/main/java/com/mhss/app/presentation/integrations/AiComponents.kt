package com.mhss.app.presentation.integrations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mhss.app.app.R
import com.mhss.app.ui.gradientBrushColor

@Composable
fun AiProviderCard(
    modifier: Modifier = Modifier,
    name: String,
    description: String,
    selected: Boolean,
    key: String,
    model: String,
    keyInfoURL: String,
    modelInfoURL: String,
    onKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onClick: () -> Unit,
    extraContent: @Composable ColumnScope.() -> Unit = {},
) {
    val colorOnBackground = MaterialTheme.colorScheme.onBackground
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorTertiary = MaterialTheme.colorScheme.tertiary
    val gradientBrush = remember {
        gradientBrushColor(
            colorOnBackground,
            colorPrimary,
            colorTertiary
        )
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        border = if (selected) BorderStroke(2.dp, gradientBrush) else null,
        onClick = onClick
    ) {
        Column(
            Modifier.padding(10.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            SavableTextField(
                text = key,
                infoURL = keyInfoURL,
                label = stringResource(R.string.api_key),
                onSave = onKeyChange
            )
            Spacer(Modifier.height(4.dp))
            SavableTextField(
                text = model,
                infoURL = modelInfoURL,
                label = stringResource(R.string.model),
                onSave = onModelChange
            )
            extraContent()
        }
    }
}

@Composable
fun SavableTextField(
    modifier: Modifier = Modifier,
    text: String,
    infoURL: String? = null,
    label: String,
    onSave: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var localText by remember { mutableStateOf("") }
    val showSave = localText != text
    LaunchedEffect(text) {
        localText = text
    }
    Column(
        modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = localText,
                onValueChange = { localText = it },
                label = { Text(text = label) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = if (infoURL != null) {
                    {
                        IconButton(onClick = { uriHandler.openUri(infoURL) }) {
                            Icon(
                                painterResource(id = R.drawable.ic_info),
                                contentDescription = null
                            )
                        }
                    }
                } else null
            )
        }
        AnimatedVisibility(showSave) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                onClick = { onSave(localText.trim()) }
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}

@Composable
fun CustomURLSection(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    url: String,
    onSave: (String) -> Unit,
    onEnable: (Boolean) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = enabled, onCheckedChange = onEnable)
            Text(
                text = stringResource(R.string.custom_url),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        AnimatedVisibility(enabled) {
            SavableTextField(
                text = url,
                label = "",
                onSave = onSave
            )
        }
    }
}