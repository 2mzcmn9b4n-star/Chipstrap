package com.chipstrap.rbx.ui.screens.fflags

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chipstrap.rbx.data.SettingsStore
import com.chipstrap.rbx.fflags.presets.FFlagPreset
import com.chipstrap.rbx.fflags.repository.FFlagEntry
import com.chipstrap.rbx.fflags.repository.FFlagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FFlagsViewModel : ViewModel() {

    private val repo = FFlagRepository()

    private val _flags = MutableStateFlow<List<FFlagEntry>>(emptyList())
    val flags: StateFlow<List<FFlagEntry>> = _flags.asStateFlow()

    fun load() {
        viewModelScope.launch { repo.load(); _flags.value = repo.flags.value }
    }

    fun add(key: String, value: String) = viewModelScope.launch {
        if (key.isNotBlank()) repo.upsert(key, value)
        _flags.value = repo.flags.value
    }

    fun delete(key: String) = viewModelScope.launch {
        repo.delete(key)
        _flags.value = repo.flags.value
    }

    fun applyPreset(preset: FFlagPreset) = viewModelScope.launch {
        repo.save(preset.flags)
        _flags.value = repo.flags.value
        SettingsStore.setLastPreset(preset.id)
    }

    fun exportTo(context: Context, uri: Uri) = viewModelScope.launch {
        val json = repo.exportJson()
        context.contentResolver.openOutputStream(uri)?.use {
            it.write(json.toByteArray())
        }
    }

    fun importFrom(context: Context, uri: Uri) = viewModelScope.launch {
        val raw = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: return@launch
        repo.importJson(raw, replace = true)
        _flags.value = repo.flags.value
    }
}
