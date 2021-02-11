package com.pawga.mynearbyapplication.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.nearby.connection.*
import com.pawga.mynearbyapplication.domain.State
import com.pawga.mynearbyapplication.utils.CodenameGenerator.generate
import timber.log.Timber

class MainViewModel(
        private val connectionsClient: ConnectionsClient?,
        private val packageName: String?
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<State>(State.RequiredPermissions)
    val stateLiveData: LiveData<State> =_stateLiveData

    private val _opponentName = MutableLiveData<String>()
    var opponentName: LiveData<String> = _opponentName

    private val _opponentEndpointId = MutableLiveData<String>()
    var opponentEndpointId: LiveData<String> = _opponentEndpointId

    val name = generate()

    private val strategyCreator = Strategy.P2P_STAR

    fun setStatus(state: State) {
        _stateLiveData.value = state
    }

    fun findOpponents() {
        startAdvertising()
        startDiscovery()
        _stateLiveData.value = State.Finding
    }

    fun disconnect() {
        val connectionsClient = connectionsClient ?: return
        connectionsClient.stopDiscovery()
        connectionsClient.stopAdvertising()
        _stateLiveData.value = State.RequiredOpponent
    }

    /** Starts looking for other players using Nearby Connections.  */
    private fun startDiscovery() {
        val packageName = packageName ?: return
        val connectionsClient = connectionsClient ?: return

        connectionsClient.startDiscovery(
                packageName, endpointDiscoveryCallback,
                DiscoveryOptions.Builder().setStrategy(strategyCreator).build())
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

    // Callbacks for receiving payloads
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            //opponentChoice = MainActivity.GameChoice.valueOf(String(payload.asBytes()!!, StandardCharsets.UTF_8))
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
//            if (update.status == PayloadTransferUpdate.Status.SUCCESS && myChoice != null && opponentChoice != null) {
//                finishRound()
//            }
        }
    }

    // Callbacks for finding other devices
    private val endpointDiscoveryCallback: EndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val connectionsClient = connectionsClient ?: return
            Timber.i("onEndpointFound: endpoint found, connecting")
            connectionsClient.requestConnection(name, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {}
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

    class Factory(private val connectionsClient: ConnectionsClient?,
                  private val packageName: String?
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(connectionsClient, packageName) as T
        }
    }
}