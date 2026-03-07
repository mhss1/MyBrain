package com.mhss.app.presentation.backup.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mhss.app.presentation.backup.BackupResult
import com.mhss.app.ui.theme.SuccessColor

@Composable
fun BackupStatusMessage(
    backupResult: BackupResult?,
    successResult: BackupResult,
    failureResult: BackupResult,
    successText: String,
    failureText: String,
    modifier: Modifier = Modifier
) {
    when (backupResult) {
        failureResult -> {
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = failureText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        successResult -> {
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SuccessColor
            ) {
                Text(
                    text = successText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }

        else -> Unit
    }
}
