package com.mhss.app.mybrain.presentation.localsync

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mhss.app.mybrain.presentation.localsync.components.CustomIpDialog
import com.mhss.app.mybrain.presentation.localsync.components.DeleteDeviceDialog
import com.mhss.app.mybrain.presentation.localsync.components.DeviceItemCard
import com.mhss.app.mybrain.presentation.localsync.components.PairDeviceDialog
import com.mhss.app.mybrain.presentation.localsync.components.PairingQrDialog
import com.mhss.app.mybrain.presentation.localsync.components.RenameDeviceDialog
import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.mybrain.sync.util.DEFAULT_SYNC_PORT
import com.mhss.app.ui.Res
import com.mhss.app.ui.active_pairings
import com.mhss.app.ui.back
import com.mhss.app.ui.copy_pairing_link
import com.mhss.app.ui.default_device_name
import com.mhss.app.ui.device_name_label
import com.mhss.app.ui.failed_to_load_camera_image
import com.mhss.app.ui.ic_edit
import com.mhss.app.ui.ic_key
import com.mhss.app.ui.ic_paste
import com.mhss.app.ui.ic_qr_code
import com.mhss.app.ui.ic_qr_scan
import com.mhss.app.ui.ic_small_arrow_down
import com.mhss.app.ui.local_ip_label
import com.mhss.app.ui.local_sync
import com.mhss.app.ui.local_sync_description
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.no_devices_paired
import com.mhss.app.ui.pairing_link_copied
import com.mhss.app.ui.paste_pairing_link
import com.mhss.app.ui.preview.BasePreview
import com.mhss.app.ui.rename_device
import com.mhss.app.ui.scan_qr_code
import com.mhss.app.ui.security_warning
import com.mhss.app.ui.show_pairing_qr
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import com.mhss.app.ui.snackbar.showErrorSnackbar
import com.mhss.app.ui.snackbar.showSuccessSnackbar
import com.mhss.app.ui.sync_conflict_handling
import com.mhss.app.ui.sync_conflict_handling_description
import com.mhss.app.ui.theme.Blue
import com.mhss.app.ui.theme.Green
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.theme.Purple
import com.mhss.app.util.clipboard.copyText
import com.mhss.app.util.clipboard.pasteText
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalSyncScreen(
    navController: NavHostController,
    pairArgs: Screen.LocalSyncScreen = Screen.LocalSyncScreen(),
    viewModel: LocalSyncViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pairedDevices = uiState.pairedDevices
    val isLoading = uiState.isLoading
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()
    RequestLocalNetworkPermission {
        viewModel.onEvent(PairedDevicesEvent.LocalNetworkPermissionGranted)
    }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showCustomIpDialogForDevice by remember { mutableStateOf<PairedDevice?>(null) }
    var showDeleteConfirmDialogForDevice by remember { mutableStateOf<PairedDevice?>(null) }
    var pendingPairArgs by remember(pairArgs) {
        mutableStateOf(
            pairArgs.takeIf {
                !it.deviceId.isNullOrBlank() &&
                        !it.ips.isNullOrBlank() &&
                        it.port != null &&
                        !it.inviteId.isNullOrBlank() &&
                        !it.inviteSecret.isNullOrBlank()
            }
        )
    }
    val qrScanLauncher = rememberQrScanLauncher { kmpBitmap ->
        if (kmpBitmap != null) {
            viewModel.onEvent(PairedDevicesEvent.DecodeAndPair(kmpBitmap))
        } else {
            scope.launch {
                uiState.snackbarHostState.showErrorSnackbar(Res.string.failed_to_load_camera_image)
            }
        }
    }

    LaunchedEffect(viewModel, clipboardManager, uiState.snackbarHostState) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PairedDevicesEffect.CopyPairingLink -> {
                    clipboardManager.copyText("pairing_link", effect.pairingLink)
                    uiState.snackbarHostState.showSuccessSnackbar(Res.string.pairing_link_copied)
                }
            }
        }
    }

    var previousDeviceCount by remember { mutableIntStateOf(pairedDevices.size) }
    LaunchedEffect(pairedDevices.size) {
        if (pairedDevices.size > previousDeviceCount && uiState.isPairingQrVisible) {
            viewModel.onEvent(PairedDevicesEvent.DismissPairingQr)
        }
        previousDeviceCount = pairedDevices.size
    }

    Scaffold(
        snackbarHost = { LocalisedSnackbarHost(uiState.snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.local_sync),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_small_arrow_down),
                            contentDescription = stringResource(Res.string.back),
                            modifier = Modifier.rotate(90f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(Res.string.local_sync_description),
                        modifier = Modifier.padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .animateContentSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(Res.string.device_name_label),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = uiState.ownDeviceName.ifBlank {
                                                stringResource(
                                                    Res.string.default_device_name
                                                )
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { showRenameDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_edit),
                                            contentDescription = stringResource(Res.string.rename_device),
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(Res.string.local_ip_label),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${uiState.ownIpAddress}:$DEFAULT_SYNC_PORT",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SyncActionTile(
                                    icon = painterResource(Res.drawable.ic_qr_code),
                                    label = stringResource(Res.string.show_pairing_qr),
                                    accent = Blue,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.onEvent(PairedDevicesEvent.ShowPairingQr)
                                    }
                                )

                                SyncActionTile(
                                    icon = painterResource(Res.drawable.ic_key),
                                    label = stringResource(Res.string.copy_pairing_link),
                                    accent = Purple,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.onEvent(PairedDevicesEvent.CopyPairingLink)
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SyncActionTile(
                                    icon = painterResource(Res.drawable.ic_qr_scan),
                                    label = stringResource(Res.string.scan_qr_code),
                                    accent = Green,
                                    modifier = Modifier.weight(1f),
                                    onClick = { qrScanLauncher() }
                                )

                                SyncActionTile(
                                    icon = painterResource(Res.drawable.ic_paste),
                                    label = stringResource(Res.string.paste_pairing_link),
                                    accent = Orange,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        scope.launch {
                                            val pairingLink = clipboardManager.pasteText()
                                            viewModel.onEvent(
                                                PairedDevicesEvent.PairFromClipboard(
                                                    pairingLink
                                                )
                                            )
                                        }
                                    }
                                )
                            }

                            Text(
                                text = stringResource(Res.string.security_warning),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.8f,
                                ),
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(Res.string.active_pairings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (pairedDevices.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(Res.string.no_devices_paired),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                } else {
                    items(pairedDevices) { device ->
                        DeviceItemCard(
                            device = device,
                            onPing = { viewModel.onEvent(PairedDevicesEvent.PingDevice(device)) },
                            onDelete = { showDeleteConfirmDialogForDevice = device },
                            onRemoveCustomIp = {
                                viewModel.onEvent(
                                    PairedDevicesEvent.SetCustomIp(
                                        device.deviceId,
                                        null
                                    )
                                )
                            },
                            onEditCustomIp = { showCustomIpDialogForDevice = device }
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.sync_conflict_handling),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(Res.string.sync_conflict_handling_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    if (uiState.isPairingQrVisible) {
        PairingQrDialog(
            qrBitmap = uiState.ownQrBitmap,
            onDismiss = { viewModel.onEvent(PairedDevicesEvent.DismissPairingQr) }
        )
    }

    if (showRenameDialog) {
        RenameDeviceDialog(
            currentName = uiState.ownDeviceName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { name ->
                viewModel.onEvent(PairedDevicesEvent.UpdateDeviceName(name))
                showRenameDialog = false
            }
        )
    }

    if (showCustomIpDialogForDevice != null) {
        CustomIpDialog(
            device = showCustomIpDialogForDevice!!,
            onDismiss = { showCustomIpDialogForDevice = null },
            onConfirm = { ip ->
                viewModel.onEvent(
                    PairedDevicesEvent.SetCustomIp(
                        showCustomIpDialogForDevice!!.deviceId,
                        ip
                    )
                )
                showCustomIpDialogForDevice = null
            }
        )
    }

    if (showDeleteConfirmDialogForDevice != null) {
        val targetDevice = showDeleteConfirmDialogForDevice!!
        DeleteDeviceDialog(
            device = targetDevice,
            onDismiss = { showDeleteConfirmDialogForDevice = null },
            onConfirm = {
                viewModel.onEvent(PairedDevicesEvent.DeleteDevice(targetDevice.deviceId))
                showDeleteConfirmDialogForDevice = null
            }
        )
    }

    pendingPairArgs?.let { request ->
        val deviceId = request.deviceId
        val rawIps = request.ips
        val port = request.port
        val inviteId = request.inviteId
        val inviteSecret = request.inviteSecret

        if (deviceId != null && rawIps != null && port != null && inviteId != null && inviteSecret != null) {
            val ips = rawIps.split(",").filter { it.isNotBlank() }
            PairDeviceDialog(
                remoteAddress = "${ips.firstOrNull().orEmpty()}:$port",
                enabled = ips.isNotEmpty(),
                onDismiss = { pendingPairArgs = null },
                onConfirm = {
                    pendingPairArgs = null
                    viewModel.onEvent(
                        PairedDevicesEvent.PairDirectly(
                            deviceId = deviceId,
                            ips = ips,
                            port = port,
                            inviteId = inviteId,
                            inviteSecret = inviteSecret
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun SyncActionTile(
    icon: Painter,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.22f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun SyncActionTilePreview() {
    BasePreview {
        Row(
            modifier = Modifier
                .width(280.dp)
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SyncActionTile(
                icon = painterResource(Res.drawable.ic_qr_code),
                label = stringResource(Res.string.show_pairing_qr),
                accent = Blue,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
            SyncActionTile(
                icon = painterResource(Res.drawable.ic_paste),
                label = stringResource(Res.string.paste_pairing_link),
                accent = Orange,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
        }
    }
}

@Preview
@Composable
fun SyncActionTilePreviewDark() {
    BasePreview(darkTheme = true) {
        Row(
            modifier = Modifier
                .width(280.dp)
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SyncActionTile(
                icon = painterResource(Res.drawable.ic_qr_scan),
                label = stringResource(Res.string.scan_qr_code),
                accent = Green,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
            SyncActionTile(
                icon = painterResource(Res.drawable.ic_key),
                label = stringResource(Res.string.copy_pairing_link),
                accent = Purple,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
        }
    }
}
