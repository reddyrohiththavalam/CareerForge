/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.local.AppDatabase
import com.example.data.model.SyncMutation
import com.example.data.model.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

sealed class SyncEngineStatus {
    data object FullySynced : SyncEngineStatus()
    data class Syncing(val pendingCount: Int) : SyncEngineStatus()
    data class OfflineBuffered(val pendingCount: Int) : SyncEngineStatus()
    data class Error(val message: String) : SyncEngineStatus()
}

class SyncManager(
    private val context: Context,
    private val db: AppDatabase,
    private val scope: CoroutineScope
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(checkInitialConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncEngineStatus>(SyncEngineStatus.FullySynced)
    val syncStatus: StateFlow<SyncEngineStatus> = _syncStatus.asStateFlow()

    private val lamportClock = AtomicLong(System.currentTimeMillis())

    private val _simulatedOffline = MutableStateFlow(false)
    val simulatedOffline: StateFlow<Boolean> = _simulatedOffline.asStateFlow()

    init {
        registerNetworkCallback()
        scope.launch(Dispatchers.IO) {
            db.syncMutationDao().getPendingMutationsFlow().collect { pending ->
                updateStatusBasedOnPending(pending.size)
            }
        }
    }

    private fun checkInitialConnectivity(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (!_simulatedOffline.value) {
                    _isOnline.value = true
                    triggerDumpingPendingBuffer()
                }
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
                scope.launch {
                    val count = db.syncMutationDao().getPendingMutationsList().size
                    _syncStatus.value = SyncEngineStatus.OfflineBuffered(count)
                }
            }
        })
    }

    fun toggleSimulatedOffline(forceOffline: Boolean) {
        _simulatedOffline.value = forceOffline
        if (forceOffline) {
            _isOnline.value = false
            scope.launch {
                val count = db.syncMutationDao().getPendingMutationsList().size
                _syncStatus.value = SyncEngineStatus.OfflineBuffered(count)
            }
        } else {
            _isOnline.value = checkInitialConnectivity()
            if (_isOnline.value) {
                triggerDumpingPendingBuffer()
            }
        }
    }

    fun nextLamportTimestamp(): Long {
        return lamportClock.incrementAndGet()
    }

    suspend fun recordLocalChange(
        entityType: String,
        recordId: String,
        operation: String,
        payloadJson: String
    ) {
        val clock = nextLamportTimestamp()
        val mutation = SyncMutation(
            entityType = entityType,
            recordId = recordId,
            operation = operation,
            payloadJson = payloadJson,
            clientLamportClock = clock,
            isDispatched = false
        )
        db.syncMutationDao().recordMutation(mutation)

        if (_isOnline.value && !_simulatedOffline.value) {
            triggerDumpingPendingBuffer()
        } else {
            val count = db.syncMutationDao().getPendingMutationsList().size
            _syncStatus.value = SyncEngineStatus.OfflineBuffered(count)
        }
    }

    fun triggerDumpingPendingBuffer() {
        scope.launch(Dispatchers.IO) {
            val pending = db.syncMutationDao().getPendingMutationsList()
            if (pending.isEmpty()) {
                _syncStatus.value = SyncEngineStatus.FullySynced
                return@launch
            }

            _syncStatus.value = SyncEngineStatus.Syncing(pending.size)
            delay(600) // Realistic CDC synchronization packet transmission to cloud

            for (mutation in pending) {
                // In production, transmits payload downstream to Supabase Realtime CDC / WebSocket
                db.syncMutationDao().markDispatched(mutation.mutationId)
            }

            db.syncMutationDao().purgeDispatched()
            _syncStatus.value = SyncEngineStatus.FullySynced
        }
    }

    private fun updateStatusBasedOnPending(pendingCount: Int) {
        if (!_isOnline.value || _simulatedOffline.value) {
            _syncStatus.value = SyncEngineStatus.OfflineBuffered(pendingCount)
        } else if (pendingCount > 0) {
            _syncStatus.value = SyncEngineStatus.Syncing(pendingCount)
        } else {
            _syncStatus.value = SyncEngineStatus.FullySynced
        }
    }
}
