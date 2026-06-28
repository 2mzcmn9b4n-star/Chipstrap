package com.chipstrap.rbx.ui.screens.optimizations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chipstrap.rbx.data.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OptimizationsViewModel : ViewModel() {

    data class State(
        val cpuGovernor: Boolean = false,
        val killBg: Boolean = false,
        val clearCache: Boolean = true,
        val disableDoze: Boolean = true,
        val gpuTuning: Boolean = true,
        val btAudio: Boolean = false,
        val memoryTrim: Boolean = true,
        val dns: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                SettingsStore.optCpuGovernor, SettingsStore.optKillBg,
                SettingsStore.optClearCache, SettingsStore.optDisableDoze,
                SettingsStore.optGpuTuning, SettingsStore.optBtAudio,
                SettingsStore.optMemoryTrim, SettingsStore.optDns
            ) { values ->
                State(
                    cpuGovernor = values[0] as Boolean,
                    killBg = values[1] as Boolean,
                    clearCache = values[2] as Boolean,
                    disableDoze = values[3] as Boolean,
                    gpuTuning = values[4] as Boolean,
                    btAudio = values[5] as Boolean,
                    memoryTrim = values[6] as Boolean,
                    dns = values[7] as Boolean
                )
            }.collect { _state.value = it }
        }
    }

    fun setCpuGovernor(v: Boolean) = viewModelScope.launch { SettingsStore.setOptCpuGovernor(v) }
    fun setKillBg(v: Boolean) = viewModelScope.launch { SettingsStore.setOptKillBg(v) }
    fun setClearCache(v: Boolean) = viewModelScope.launch { SettingsStore.setOptClearCache(v) }
    fun setDisableDoze(v: Boolean) = viewModelScope.launch { SettingsStore.setOptDisableDoze(v) }
    fun setGpuTuning(v: Boolean) = viewModelScope.launch { SettingsStore.setOptGpuTuning(v) }
    fun setBtAudio(v: Boolean) = viewModelScope.launch { SettingsStore.setOptBtAudio(v) }
    fun setMemoryTrim(v: Boolean) = viewModelScope.launch { SettingsStore.setOptMemoryTrim(v) }
    fun setDns(v: Boolean) = viewModelScope.launch { SettingsStore.setOptDns(v) }
}
