package com.chipstrap.rbx.ui.screens.integrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chipstrap.rbx.data.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class IntegrationsViewModel : ViewModel() {

    data class State(
        val strategy: String = "shizuku",
        val preferredApp: String = "global",
        val customPackage: String = ""
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(SettingsStore.injectionStrategy, SettingsStore.preferredRobloxApp, SettingsStore.customRobloxPackage) { strat, app, custom ->
                State(strat, app, custom)
            }.collect { _state.value = it }
        }
    }

    fun setStrategy(id: String) = viewModelScope.launch { SettingsStore.setInjectionStrategy(id) }
    fun setPreferredApp(id: String) = viewModelScope.launch { SettingsStore.setPreferredRobloxApp(id) }
    fun setCustomPackage(pkg: String) = viewModelScope.launch { SettingsStore.setCustomRobloxPackage(pkg) }
}
