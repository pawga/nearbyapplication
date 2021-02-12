package com.pawga.mynearbyapplication.ui.main

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.nearby.connection.*
import com.pawga.mynearbyapplication.domain.State
import timber.log.Timber
import java.nio.charset.StandardCharsets

class MainViewModel(
    private val connectionsClient: ConnectionsClient?,
    private val packageName: String?
) : ViewModel() {


    private val _stateLiveData = MutableLiveData<State>(State.RequiredPermissions)
    private val _opponentName = MutableLiveData<String>()
    private val _opponentEndpointId = MutableLiveData<String>()

    val stateLiveData: LiveData<State> =_stateLiveData
    var opponentName: LiveData<String> = _opponentName
    var opponentEndpointId: LiveData<String> = _opponentEndpointId
    val message = MutableLiveData<String>()
    val name = Build.MODEL

    private val strategyCreator = Strategy.P2P_STAR

    // Callbacks for receiving payloads
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {

        var message: String = ""
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            message = String(bytes, StandardCharsets.UTF_8)
            Timber.d("Received: $endpointId: $message")
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS
                    && message.isNotEmpty()
                    && endpointId == _opponentEndpointId.value) {
                _stateLiveData.value = State.ReceivedData(message)
                message = ""
            }
            Timber.d("onPayloadTransferUpdate: ${update.status}")
        }
    }

    // Callbacks for finding other devices
    private val endpointDiscoveryCallback: EndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val connectionsClient = connectionsClient ?: return
            Timber.i("onEndpointFound: endpoint found, connecting")
            connectionsClient.requestConnection(name, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            disconnect()
        }
    }

    // Callbacks for connections to other devices
    private val connectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Timber.i("onConnectionInitiated: accepting connection")
            val connectionsClient = connectionsClient ?: return
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            _opponentName.value = connectionInfo.endpointName
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Timber.i("onConnectionResult: connection successful")
                val connectionsClient = connectionsClient ?: return
                connectionsClient.stopDiscovery()
                connectionsClient.stopAdvertising()
                _opponentEndpointId.value = endpointId
                _stateLiveData.value = State.Connected
            } else {
                _stateLiveData.value = State.Error("onConnectionResult: connection failed")
                Timber.i("onConnectionResult: connection failed")
            }
        }

        override fun onDisconnected(endpointId: String) {
            _opponentEndpointId.value = null
            _opponentName.value = null
            _stateLiveData.value = State.RequiredOpponent
            Timber.i("onDisconnected: disconnected from the opponent")
        }
    }

    fun setStatus(state: State) {
        _stateLiveData.value = state
    }

    fun send() {
        val outMessage = message.value ?: return
        val opponentEndpointId = opponentEndpointId.value ?: return

        connectionsClient?.sendPayload(
                opponentEndpointId, Payload.fromBytes(outMessage.toByteArray(StandardCharsets.UTF_8))
        )
        _stateLiveData.value = State.SentData(outMessage)
        message.value = null
    }

    fun findOpponents() {
        startAdvertising()
        startDiscovery()
        _stateLiveData.value = State.Finding
    }

    fun stopFinding() {
        disconnect()
    }

    fun disconnect() {
        val connectionsClient = connectionsClient ?: return
        connectionsClient.stopDiscovery()
        connectionsClient.stopAdvertising()
        _stateLiveData.value = State.RequiredOpponent
        _opponentName.value = null
    }

    /** Starts looking for other players using Nearby Connections.  */
    private fun startDiscovery() {
        val packageName = packageName ?: return
        val connectionsClient = connectionsClient ?: return

        connectionsClient.startDiscovery(
            packageName, endpointDiscoveryCallback,
            DiscoveryOptions.Builder().setStrategy(strategyCreator).build()
        )
    }

    /** Broadcasts our presence using Nearby Connections so other players can find us.  */
    private fun startAdvertising() {
        val packageName = packageName ?: return
        val connectionsClient = connectionsClient ?: return
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startAdvertising(
            name,
            packageName,
            connectionLifecycleCallback,
            AdvertisingOptions
                .Builder()
                .setStrategy(strategyCreator)
                .build()
        )
    }

    class Factory(
        private val connectionsClient: ConnectionsClient?,
        private val packageName: String?
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(connectionsClient, packageName) as T
        }
    }
}