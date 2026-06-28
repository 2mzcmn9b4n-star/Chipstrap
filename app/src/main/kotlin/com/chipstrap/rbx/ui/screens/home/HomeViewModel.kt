package com.chipstrap.rbx.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chipstrap.rbx.data.SettingsStore
import com.chipstrap.rbx.fflags.repository.FFlagRepository
import com.chipstrap.rbx.fflags.strategies.LocalProfileStrategy
import com.chipstrap.rbx.fflags.strategies.StrategyResolver
import com.chipstrap.rbx.roblox.RobloxPackages
import com.chipstrap.rbx.service.LauncherForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val fflags = FFlagRepository()

    private val _isInstalled = MutableStateFlow(false)
    val isInstalled: StateFlow<Boolean> = _isInstalled.asStateFlow()

    private val _fflagsCount = MutableStateFlow(0)
    val fflagsCount: StateFlow<Int> = _fflagsCount.asStateFlow()

    private val _activePreset = MutableStateFlow("")
    val activePreset: StateFlow<String> = _activePreset.asStateFlow()

    private val _lastLaunch = MutableStateFlow("")
    val lastLaunch: StateFlow<String> = _lastLaunch.asStateFlow()

    private val _isLaunching = MutableStateFlow(false)
    val isLaunching: StateFlow<Boolean> = _isLaunching.asStateFlow()

    fun refresh(context: Context) {
        viewModelScope.launch {
            val pkg = RobloxPackages.resolvePreferred(context)
            _isInstalled.value = RobloxPackages.isInstalled(context, pkg)
            fflags.load()
            _fflagsCount.value = fflags.count()
            _activePreset.value = SettingsStore.lastPreset.first()
            _lastLaunch.value = SettingsStore.lastLaunchTs.first()
        }
    }

    fun strategySummary(): String {
        // Synchronous summary for the home card. Returns the user's preferred
        // strategy label; the resolver runs at launch time for real availability.
        var label = "—"
        // Trampoline to avoid blocking UI
        viewModelScope.launch {
            val id = SettingsStore.injectionStrategy.first()
            label = when (id) {
                "shizuku" -> "Shizuku"
                "root" -> "Root"
                "virtual" -> "Virtual space"
                else -> "Local profile"
            }
        }
        return label
    }

    fun launch(context: Context) {
        if (_isLaunching.value) return
        _isLaunching.value = true
        val intent = Intent(context, LauncherForegroundService::class.java).apply {
            action = LauncherForegroundService.ACTION_LAUNCH
        }
        androidx.core.content.ContextCompat.startForegroundService(context, intent)
        // Optimistic — the real launch happens in the service. Reset the spinner after a bit.
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            _isLaunching.value = false
            refresh(context)
        }
    }
}
