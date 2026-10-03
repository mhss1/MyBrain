package com.mhss.app.mybrain.presentation.localsync

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect class KmpBitmap

@Composable
expect fun KmpImage(
    bitmap: KmpBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier
)

@Composable
expect fun rememberQrScanLauncher(onResult: (KmpBitmap?) -> Unit): () -> Unit

@Composable
expect fun RequestLocalNetworkPermission(onGranted: () -> Unit)

interface QrCodeUtils {
    fun generateQrCode(content: String, size: Int = 512): KmpBitmap
    fun decodeQrFromBitmap(bitmap: KmpBitmap): String?
}
