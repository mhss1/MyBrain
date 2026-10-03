package com.mhss.app.mybrain.sync.util

import com.mhss.app.mybrain.sync.model.SyncTriggerMessage
import kotlin.time.Duration.Companion.days

const val DEFAULT_SYNC_PORT = 38300
const val SYNC_PAGE_SIZE = 150
const val PAIRING_INVITE_DURATION_MS = 5 * 60 * 1000L

const val TOMBSTONE_CLEANUP_BATCH_SIZE = 500
const val TOMBSTONE_CLEANUP_MAX_ATTEMPTS = 3
const val TOMBSTONE_CLEANUP_WORK_NAME = "tombstone_cleanup_work"
val TOMBSTONE_RETENTION = 90.days
val TOMBSTONE_CLEANUP_INTERVAL = 1.days

const val ROUTE_PAIR = "/pair"
const val ROUTE_PING = "/ping"
const val ROUTE_SYNC = "/sync"

const val PARAM_DEVICE_ID = "deviceId"
const val PARAM_IPS = "ips"
const val PARAM_PORT = "port"
const val PARAM_INVITE_ID = "inviteId"
const val PARAM_INVITE_SECRET = "inviteSecret"
const val SYNC_DEEP_LINK_BASE_URI = "mybrain://sync"
const val SYNC_DEEP_LINK_PATTERN = "$SYNC_DEEP_LINK_BASE_URI?$PARAM_DEVICE_ID={$PARAM_DEVICE_ID}&$PARAM_IPS={$PARAM_IPS}&$PARAM_PORT={$PARAM_PORT}&$PARAM_INVITE_ID={$PARAM_INVITE_ID}&$PARAM_INVITE_SECRET={$PARAM_INVITE_SECRET}"
val SYNC_TRIGGER_MESSAGE =
    SyncTriggerMessage
