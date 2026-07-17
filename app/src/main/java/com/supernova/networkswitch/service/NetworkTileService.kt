package com.supernova.networkswitch.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.telephony.SubscriptionManager
import com.supernova.networkswitch.domain.model.ControlMethod
import com.supernova.networkswitch.domain.model.NetworkMode
import com.supernova.networkswitch.domain.model.ToggleModeConfig
import com.supernova.networkswitch.domain.usecase.GetCurrentNetworkModeUseCase
import com.supernova.networkswitch.domain.usecase.ToggleNetworkModeUseCase
import com.supernova.networkswitch.domain.usecase.GetToggleModeConfigUseCase
import com.supernova.networkswitch.domain.repository.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class NetworkTileService : TileService() {

    @Inject
    lateinit var getCurrentNetworkModeUseCase: GetCurrentNetworkModeUseCase

    @Inject
    lateinit var toggleNetworkModeUseCase: ToggleNetworkModeUseCase

    @Inject
    lateinit var getToggleModeConfigUseCase: GetToggleModeConfigUseCase

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var currentNetworkMode: NetworkMode? = null
    private var toggleConfig: ToggleModeConfig? = null

    override fun onStartListening() {
        super.onStartListening()
        serviceScope.launch {
            try {
                preferencesRepository.observeToggleModeConfig().collect { newConfig ->
                    toggleConfig = newConfig
                    refreshNetworkState()
                }
            } catch (_: Exception) {
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()

        val subId = SubscriptionManager.getDefaultDataSubscriptionId()

        serviceScope.launch {
            try {
                toggleNetworkModeUseCase(subId)
                    .onSuccess { newMode ->
                        currentNetworkMode = newMode
                        withContext(Dispatchers.Main) {
                            updateTile()
                        }
                    }
                    .onFailure {
                        refreshNetworkState()
                    }
            } catch (_: Exception) {
                refreshNetworkState()
            }
        }
    }

    private suspend fun refreshNetworkState() {
        val subId = SubscriptionManager.getDefaultDataSubscriptionId()

        try {
            getCurrentNetworkModeUseCase(subId)
                .onSuccess { networkMode ->
                    currentNetworkMode = networkMode
                    withContext(Dispatchers.Main) {
                        updateTile()
                    }
                }
        } catch (_: Exception) {
        }
    }

    private fun updateTile() {
        try {
            qsTile?.apply {
                val config = toggleConfig

                if (config != null) {
                    state = Tile.STATE_ACTIVE
                    val currentMode = config.getCurrentMode()
                    val nextMode = config.getNextMode()
                    label = currentMode.displayName
                    subtitle = nextMode.displayName
                } else {
                    state = Tile.STATE_INACTIVE
                    label = "网络模式"
                    subtitle = "配置未加载"
                }
                updateTile()
            }
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
