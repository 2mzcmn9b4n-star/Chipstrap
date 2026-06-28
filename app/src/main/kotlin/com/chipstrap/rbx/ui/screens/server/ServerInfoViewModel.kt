package com.chipstrap.rbx.ui.screens.server

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chipstrap.rbx.roblox.RobloxPackages
import com.chipstrap.rbx.server.ServerInfoProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServerInfoViewModel : ViewModel() {

    private val provider = ServerInfoProvider()
    private val _info = MutableStateFlow<ServerInfoProvider.ServerInfo?>(null)
    val info: StateFlow<ServerInfoProvider.ServerInfo?> = _info.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun refresh(context: Context) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val pkg = RobloxPackages.resolvePreferred(context)
                val s = provider.pollFromRobloxLogs(pkg)
                if (s != null) {
                    val host = s.host
                    val loc = if (host != null) provider.lookupLocation(host) else null
                    val ping = if (host != null) provider.ping(host) else null
                    _info.value = s.copy(location = loc, pingMs = ping)
                } else {
                    _info.value = null
                }
            } finally {
                _loading.value = false
            }
        }
    }
}
