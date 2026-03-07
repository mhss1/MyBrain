package com.mhss.app.presentation.backup.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mhss.app.presentation.backup.BackupResult
import com.mhss.app.presentation.backup.toUiMessage
import com.mhss.app.ui.R
import com.mhss.app.ui.theme.SuccessColor

@Composable
fun BackupStatusMessage(
    backupResult: BackupResult,
    modifier: Modifier = Modifier
) {
    Column(Modifier.animateContentSize()) {
        when (backupResult) {
            is BackupResult.Error -> {
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = backupResult.error.toUiMessage(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            BackupResult.ExportSuccess,
            BackupResult.ImportSuccess -> {
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessColor
                ) {
                    Text(
                        text = stringResource(
                            when (backupResult) {
                                BackupResult.ExportSuccess -> R.string.export_success
                                BackupResult.ImportSuccess -> R.string.import_success
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            BackupResult.Idle,
            BackupResult.Loading -> Unit
        }
    }
}
