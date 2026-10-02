package com.mhss.app.mybrain.sync

import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.database.sync.LocalChangeObserver
import com.mhss.app.mybrain.sync.client.LocalSyncClient
import com.mhss.app.mybrain.sync.domain.DiscoveredDevice
import com.mhss.app.mybrain.sync.domain.MergePayloadUseCase
import com.mhss.app.mybrain.sync.domain.NetworkDiscoveryManager
import com.mhss.app.mybrain.sync.domain.NetworkHelper
import com.mhss.app.mybrain.sync.domain.findReachableIp
import com.mhss.app.mybrain.sync.model.ChangesMessage
import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.mybrain.sync.model.RequestChangesMessage
import com.mhss.app.mybrain.sync.model.SyncErrorMessage
import com.mhss.app.mybrain.sync.model.SyncPayload
import com.mhss.app.mybrain.sync.model.SyncSocketMessage
import com.mhss.app.mybrain.sync.model.SyncTriggerMessage
import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import com.mhss.app.mybrain.sync.repository.SyncRepository
import com.mhss.app.mybrain.sync.server.LocalSyncServer
import com.mhss.app.mybrain.sync.server.PairRouteHandler
import com.mhss.app.mybrain.sync.server.PingRouteHandler
import com.mhss.app.mybrain.sync.server.SyncWebSocketHandler
import com.mhss.app.mybrain.sync.util.CompressionManager
import com.mhss.app.mybrain.sync.util.DEFAULT_SYNC_PORT
import com.mhss.app.mybrain.sync.util.EncryptionManager
import com.mhss.app.mybrain.sync.util.SYNC_PAGE_SIZE
import com.mhss.app.mybrain.sync.util.SYNC_TRIGGER_MESSAGE
import com.mhss.app.mybrain.sync.util.concurrentMutableMap
import com.mhss.app.mybrain.sync.util.receiveEncrypted
import com.mhss.app.mybrain.sync.util.sendEncrypted
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Single
class SyncOrchestrator(
    private val deviceKeyStore: DeviceKeyStore,
    private val pairedDevicesRepository: PairedDevicesRepository,
    private val syncRepository: SyncRepository,
    private val localChangeObserver: LocalChangeObserver,
    private val transactionProvider: DatabaseTransactionProvider,
    private val server: LocalSyncServer,
    private val client: LocalSyncClient,
    private val pairRouteHandler: PairRouteHandler,
    private val pingRouteHandler: PingRouteHandler,
    private val syncWebSocketHandler: SyncWebSocketHandler,
    private val mergePayload: MergePayloadUseCase,
    private val networkHelper: NetworkHelper,
    private val networkDiscoveryManager: NetworkDiscoveryManager,
    private val compressor: CompressionManager,
    private val json: Json,
    private val encryptionManager: EncryptionManager,
) {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)
    private val syncMutex = Mutex()

    init {
        scope.launch {
            try {
                val devices = pairedDevicesRepository.getPairedDevices()
                devices.forEach { device ->
                    if (device.isConnected) {
                        updateConnectionStatus(device.deviceId, false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            initializeNetworkDiscovery()

            @OptIn(FlowPreview::class)
            networkHelper.observeNetworkChanges()
                .distinctUntilChanged()
                .drop(1)
                .debounce(1000L.milliseconds)
                .collect { currentIps ->
                    if (currentIps.isNotEmpty()) {
                        broadcastNewIpsToPeers()
                    }
                }
        }
        pingRouteHandler.onPingReceived = { peerDeviceId ->
            scope.launch {
                connectWebSocket(peerDeviceId)
                syncDevice(peerDeviceId)
            }
        }
        pairRouteHandler.onPairingAccepted = { peerDeviceId ->
            disconnectSessions(peerDeviceId)
        }
        syncWebSocketHandler.onMessageReceived = { peerDeviceId, message, session ->
            scope.launch {
                handleSocketMessage(peerDeviceId, message, session)
            }
        }
        syncWebSocketHandler.onPeerConnected = { peerDeviceId, session ->
            scope.launch {
                if (!syncWebSocketHandler.isCurrentSession(peerDeviceId, session)) {
                    return@launch
                }
                if (!shouldKeepOutgoing(peerDeviceId)) {
                    activePeerJobs.remove(peerDeviceId)?.cancel()
                }
                if (resolveIncomingConnection(peerDeviceId, session)) {
                    updateConnectionStatus(peerDeviceId, true)
                }
            }
        }
        syncWebSocketHandler.onPeerDisconnected = { peerDeviceId, _ ->
            scope.launch {
                updateConnectionStatus(peerDeviceId, false)
            }
        }
    }

    private val activeClientConnections =
        concurrentMutableMap<String, DefaultClientWebSocketSession>()
    private val clientConnectionsMutex = Mutex()
    private val activePeerJobs = concurrentMutableMap<String, Job>()
    private var serverJob: Job? = null
    private var dbObservationJob: Job? = null

    fun startNetworkDiscovery() {
        scope.launch {
            initializeNetworkDiscovery()
        }
    }

    private suspend fun initializeNetworkDiscovery() {
        val deviceId = deviceKeyStore.getCurrentDeviceId()
        networkDiscoveryManager.registerService(deviceId, DEFAULT_SYNC_PORT)
        networkDiscoveryManager.startDiscovery { device ->
            scope.launch {
                handleDiscoveredDevice(device)
            }
        }
    }

    private suspend fun handleDiscoveredDevice(device: DiscoveredDevice) {
        val pairedDevice = pairedDevicesRepository.getPairedDevice(device.deviceId) ?: return
        if (pairedDevice.ipAddress == device.ipAddress) return

        val pingSuccess = client.ping(
            device.ipAddress,
            pairedDevice.port,
            pairedDevice.deviceId
        )
        if (pingSuccess) {
            connectWebSocket(pairedDevice.deviceId)
        }
    }

    fun startServer(port: Int = DEFAULT_SYNC_PORT) {
        if (serverJob?.isActive != true) {
            serverJob = scope.launch {
                try {
                    server.start(port)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        startObservingChanges()
    }

    fun stopServerIfNoPairedDevices() {
        scope.launch {
            if (pairedDevicesRepository.getPairedDevices().isNotEmpty()) return@launch
            serverJob?.cancelAndJoin()
            serverJob = null
            dbObservationJob?.cancelAndJoin()
            dbObservationJob = null
            server.stop()
        }
    }

    fun syncAllAsync() {
        scope.launch {
            val pairedDevices = pairedDevicesRepository.getPairedDevices()
            for (peer in pairedDevices) {
                try {
                    connectWebSocket(peer.deviceId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun manualSync(deviceId: String): Boolean = withContext(Dispatchers.IO) {
        val pairedDevices = pairedDevicesRepository.getPairedDevices()
        val peer = pairedDevices.firstOrNull { it.deviceId == deviceId } ?: return@withContext false
        try {
            val reachableIp = networkHelper.findReachableIp(peer) ?: peer.ipAddress

            if (reachableIp != peer.ipAddress) {
                pairedDevicesRepository.updateIpAddresses(
                    peer.deviceId,
                    reachableIp,
                    peer.candidateIpAddresses
                )
            }

            val pingSuccess = client.ping(
                reachableIp,
                peer.port,
                deviceId
            )
            if (pingSuccess) {
                connectWebSocket(deviceId)
                syncDevice(deviceId)
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    suspend fun syncDevice(deviceId: String) = withContext(Dispatchers.IO) {
        clientConnectionsMutex.withLock {
            activeClientConnections[deviceId]
        }?.let { session ->
            requestChanges(deviceId, session)
            return@withContext
        }

        if (syncWebSocketHandler.sendToPeer(deviceId, buildRequestChangesMessage(deviceId))) {
            return@withContext
        }
    }


    fun connectWebSocket(deviceId: String) {
        if (activeClientConnections.containsKey(deviceId)) return

        activePeerJobs[deviceId]?.cancel()
        activePeerJobs[deviceId] = scope.launch {
            if (syncWebSocketHandler.isPeerConnected(deviceId) && !shouldKeepOutgoing(deviceId)) {
                activePeerJobs.remove(deviceId)
                return@launch
            }

            var delayDuration = 5.seconds
            var failureCount = 0

            while (isActive) {
                val peer = pairedDevicesRepository.getPairedDevice(deviceId) ?: return@launch
                if (syncWebSocketHandler.isPeerConnected(peer.deviceId) && !shouldKeepOutgoing(peer.deviceId)) {
                    activePeerJobs.remove(peer.deviceId)
                    return@launch
                }
                try {
                    connectToPeer(peer)
                    delayDuration = 10.seconds
                    failureCount = 0
                } catch (e: Exception) {
                    failureCount++
                    if (failureCount >= 3) {
                        updateConnectionStatus(peer.deviceId, false)
                        val alt = networkHelper.findReachableIp(peer)
                        if (alt != null) {
                            pairedDevicesRepository.updateIpAddresses(
                                peer.deviceId,
                                alt,
                                peer.candidateIpAddresses
                            )
                            failureCount = 0
                            continue
                        }
                    }
                    e.printStackTrace()
                }
                delay(delayDuration)
                delayDuration = (delayDuration * 2).coerceAtMost(5.minutes)
            }
        }
    }

    private suspend fun connectToPeer(peer: PairedDevice) {
        val currentDeviceId = deviceKeyStore.getCurrentDeviceId()
        try {
            val peerKey = deviceKeyStore.getDeviceKey(peer.deviceId)
            if (peerKey != null) {
                client.connectWebSocket(peer.ipAddress, peer.port, currentDeviceId) {
                    val session = this
                    val registered = clientConnectionsMutex.withLock {
                        if (activeClientConnections.containsKey(peer.deviceId)) {
                            false
                        } else {
                            activeClientConnections[peer.deviceId] = session
                            true
                        }
                    }
                    if (!registered) {
                        session.close()
                        return@connectWebSocket
                    }
                    try {
                        if (!resolveOutgoingConnection(peer.deviceId, session)) {
                            return@connectWebSocket
                        }

                        updateConnectionStatus(peer.deviceId, true)
                        requestChanges(peer.deviceId, session)

                        sendEncrypted(
                            SYNC_TRIGGER_MESSAGE,
                            encryptionManager,
                            peerKey,
                            compressor,
                            json
                        )

                        while (true) {
                            val message =
                                receiveEncrypted(encryptionManager, peerKey, compressor, json)
                            handleSocketMessage(peer.deviceId, message, this)
                        }
                    } finally {
                        val removed = clientConnectionsMutex.withLock {
                            if (activeClientConnections[peer.deviceId] === session) {
                                activeClientConnections.remove(peer.deviceId)
                                true
                            } else {
                                false
                            }
                        }
                        if (removed) {
                            updateConnectionStatus(peer.deviceId, false)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @OptIn(FlowPreview::class)
    private fun startObservingChanges() {
        dbObservationJob?.cancel()
        dbObservationJob = scope.launch {
            localChangeObserver.changes.collect {
                broadcastSyncTrigger()
            }
        }
    }

    private suspend fun broadcastSyncTrigger(excludeDeviceId: String? = null) {
        val clientConnections = clientConnectionsMutex.withLock {
            activeClientConnections.toMap()
        }
        clientConnections.forEach { (peerDeviceId, session) ->
            if (peerDeviceId != excludeDeviceId) {
                runCatching {
                    val peerKey = deviceKeyStore.getDeviceKey(peerDeviceId) ?: return@runCatching
                    session.sendEncrypted(
                        SYNC_TRIGGER_MESSAGE,
                        encryptionManager,
                        peerKey,
                        compressor,
                        json
                    )
                }
            }
        }
        val excludeIds = if (excludeDeviceId != null) {
            clientConnections.keys + excludeDeviceId
        } else {
            clientConnections.keys
        }
        syncWebSocketHandler.broadcastToAll(excludeDeviceIds = excludeIds)
    }

    private suspend fun shouldKeepOutgoing(peerDeviceId: String): Boolean {
        return deviceKeyStore.getCurrentDeviceId() < peerDeviceId
    }

    private suspend fun resolveOutgoingConnection(
        peerDeviceId: String,
        outgoingSession: DefaultClientWebSocketSession
    ): Boolean {
        val incomingSession = syncWebSocketHandler.getSession(peerDeviceId) ?: return true
        if (shouldKeepOutgoing(peerDeviceId)) {
            syncWebSocketHandler.closeSession(peerDeviceId, incomingSession)
            return true
        }
        removeClientConnection(peerDeviceId, outgoingSession)
        outgoingSession.close()
        return false
    }

    private suspend fun resolveIncomingConnection(
        peerDeviceId: String,
        incomingSession: DefaultWebSocketServerSession
    ): Boolean {
        if (!syncWebSocketHandler.isCurrentSession(peerDeviceId, incomingSession)) {
            return false
        }
        val outgoingSession = clientConnectionsMutex.withLock {
            activeClientConnections[peerDeviceId]
        } ?: return true
        if (shouldKeepOutgoing(peerDeviceId)) {
            syncWebSocketHandler.closeSession(peerDeviceId, incomingSession)
            return false
        }
        if (removeClientConnection(peerDeviceId, outgoingSession)) {
            outgoingSession.close()
        }
        return syncWebSocketHandler.isCurrentSession(peerDeviceId, incomingSession)
    }

    private suspend fun removeClientConnection(
        peerDeviceId: String,
        expectedSession: DefaultClientWebSocketSession
    ): Boolean {
        return clientConnectionsMutex.withLock {
            if (activeClientConnections[peerDeviceId] === expectedSession) {
                activeClientConnections.remove(peerDeviceId)
                true
            } else {
                false
            }
        }
    }

    private suspend fun handleSocketMessage(
        peerDeviceId: String,
        message: SyncSocketMessage,
        session: WebSocketSession
    ) {
        try {
            when (message) {
                is SyncTriggerMessage -> {
                    requestChanges(peerDeviceId, session)
                }

                is RequestChangesMessage -> {
                    sendChanges(peerDeviceId, message.lastSyncedSeq, session)
                }

                is ChangesMessage -> syncMutex.withLock {
                    if (!canApplyChanges(peerDeviceId, message.lastSyncedSeq)) {
                        requestChanges(peerDeviceId, session)
                    } else {
                        val applied = applyChanges(peerDeviceId, message.payload)
                        if (applied) {
                            requestChanges(peerDeviceId, session)
                        }
                    }
                }

                is SyncErrorMessage -> Unit
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sendSocketError(peerDeviceId, session, e.message ?: "Unknown socket sync error")
        }
    }

    private suspend fun requestChanges(peerDeviceId: String, session: WebSocketSession) {
        val peerKey = deviceKeyStore.getDeviceKey(peerDeviceId) ?: return
        session.sendEncrypted(
            buildRequestChangesMessage(peerDeviceId),
            encryptionManager,
            peerKey,
            compressor,
            json
        )
    }

    private suspend fun buildRequestChangesMessage(peerDeviceId: String): SyncSocketMessage {
        val peer = pairedDevicesRepository.getPairedDevice(peerDeviceId)
        return RequestChangesMessage(
            lastSyncedSeq = peer?.lastSyncedSeq ?: 0L,
            sourceIps = networkHelper.getAllLocalIpAddresses()
        )
    }

    private suspend fun sendChanges(
        peerDeviceId: String,
        lastSyncedSeq: Long,
        session: WebSocketSession
    ) {
        val peerKey = deviceKeyStore.getDeviceKey(peerDeviceId) ?: return
        if (lastSyncedSeq < 0L || lastSyncedSeq > syncRepository.getMaxSyncSequence()) {
            sendSocketError(peerDeviceId, session, "Invalid sync sequence")
            return
        }
        pairedDevicesRepository.updateLastAcknowledgedLocalSeq(peerDeviceId, lastSyncedSeq)
        val payload = buildSyncPayload(lastSyncedSeq)
        session.sendEncrypted(
            ChangesMessage(
                lastSyncedSeq = lastSyncedSeq,
                payload = payload
            ),
            encryptionManager,
            peerKey,
            compressor,
            json
        )
    }

    private suspend fun buildSyncPayload(lastSyncedSeq: Long): SyncPayload {
        val currentDbMaxSeq = syncRepository.getMaxSyncSequence()
        val nextSequences = syncRepository.getNextSyncSequences(
            seq = lastSyncedSeq,
            maxSeq = currentDbMaxSeq,
            limit = SYNC_PAGE_SIZE + 1
        )
        val hasMore = nextSequences.size > SYNC_PAGE_SIZE
        val pageEndSeq = if (hasMore) {
            nextSequences[SYNC_PAGE_SIZE - 1]
        } else {
            currentDbMaxSeq
        }
        return SyncPayload(
            notes = syncRepository.getNotesAfterSeq(lastSyncedSeq, pageEndSeq),
            folders = syncRepository.getNoteFoldersAfterSeq(lastSyncedSeq, pageEndSeq),
            tasks = syncRepository.getTasksAfterSeq(lastSyncedSeq, pageEndSeq),
            diaryEntries = syncRepository.getDiaryEntriesAfterSeq(lastSyncedSeq, pageEndSeq),
            bookmarks = syncRepository.getBookmarksAfterSeq(lastSyncedSeq, pageEndSeq),
            assistantThreads = syncRepository.getThreadsAfterSeq(lastSyncedSeq, pageEndSeq),
            assistantMessages = syncRepository.getMessagesAfterSeq(lastSyncedSeq, pageEndSeq),
            deletedEntities = syncRepository.getDeletedEntitiesAfterSeq(
                lastSyncedSeq,
                pageEndSeq
            ),
            maxSequence = maxOf(lastSyncedSeq, pageEndSeq),
            hasMore = hasMore
        )
    }

    private suspend fun applyChanges(
        peerDeviceId: String,
        payload: SyncPayload
    ): Boolean {
        val applied = transactionProvider.runInTransaction {
            val peer = pairedDevicesRepository.getPairedDevice(peerDeviceId)
                ?: return@runInTransaction false
            if (payload.maxSequence <= peer.lastSyncedSeq) {
                return@runInTransaction false
            }
            mergePayload(payload)
            pairedDevicesRepository.updateLastSyncedSeq(peerDeviceId, payload.maxSequence)
            true
        }
        if (applied) {
            broadcastSyncTrigger(excludeDeviceId = peerDeviceId)
        }
        return applied
    }

    private suspend fun canApplyChanges(
        peerDeviceId: String,
        payloadLastSyncedSeq: Long?
    ): Boolean {
        val peer = pairedDevicesRepository.getPairedDevice(peerDeviceId) ?: return false
        return payloadLastSyncedSeq == peer.lastSyncedSeq
    }

    private suspend fun sendSocketError(
        peerDeviceId: String,
        session: WebSocketSession,
        error: String
    ) {
        val peerKey = deviceKeyStore.getDeviceKey(peerDeviceId) ?: return
        session.sendEncrypted(
            SyncErrorMessage(error),
            encryptionManager,
            peerKey,
            compressor,
            json
        )
    }

    private suspend fun updateConnectionStatus(deviceId: String, isConnected: Boolean) {
        if (isConnected) {
            pairedDevicesRepository.updateConnectionStatus(deviceId, true)
        } else {
            val hasClientConnection = activeClientConnections.containsKey(deviceId)
            val hasServerConnection = syncWebSocketHandler.isPeerConnected(deviceId)
            if (!hasClientConnection && !hasServerConnection) {
                pairedDevicesRepository.updateConnectionStatus(deviceId, false)
                deviceKeyStore.getDeviceKey(deviceId)?.let(encryptionManager::removeKey)
            }
        }
    }

    private suspend fun broadcastNewIpsToPeers() = withContext(Dispatchers.IO) {
        try {
            val pairedDevices = pairedDevicesRepository.getPairedDevices()
            for (peer in pairedDevices) {
                activePeerJobs[peer.deviceId]?.cancel()
                launch {
                    try {
                        val reachableIp = networkHelper.findReachableIp(peer) ?: peer.ipAddress
                        client.ping(
                            reachableIp,
                            peer.port,
                            peer.deviceId
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        connectWebSocket(peer.deviceId)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnectDevice(deviceId: String) {
        scope.launch { disconnectSessions(deviceId) }
    }

    private suspend fun disconnectSessions(deviceId: String) {
        activePeerJobs.remove(deviceId)?.cancel()
        deviceKeyStore.getDeviceKey(deviceId)?.let(encryptionManager::removeKey)
        val clientSession = clientConnectionsMutex.withLock {
            activeClientConnections.remove(deviceId)
        }
        clientSession?.close()
        syncWebSocketHandler.closeSession(deviceId)
    }
}
