package com.chipstrap.rbx.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "chipstrap_settings")

/**
 * Persistent settings store. Backed by Jetpack DataStore.
 * Holds preferences that are NOT FFlags (FFlags are stored as JSON in [AppPaths.clientAppSettingsFile]).
 */
object SettingsStore {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    object Keys {
        val PREFERRED_ROBLOX_APP = stringPreferencesKey("preferred_roblox_app")
        val CUSTOM_ROBLOX_PACKAGE = stringPreferencesKey("custom_roblox_package")
        val THEME = stringPreferencesKey("theme") // dark/light/system
        val INJECTION_STRATEGY = stringPreferencesKey("injection_strategy")
        val ALLOW_MANAGE_FFLAGS = booleanPreferencesKey("allow_manage_fflags")
        val LAST_PRESET = stringPreferencesKey("last_preset")
        val LAST_LAUNCH_TS = stringPreferencesKey("last_launch_ts")

        // Optimizations
        val OPT_CPU_GOVERNOR = booleanPreferencesKey("opt_cpu_governor")
        val OPT_KILL_BG = booleanPreferencesKey("opt_kill_bg")
        val OPT_CLEAR_CACHE = booleanPreferencesKey("opt_clear_cache")
        val OPT_DISABLE_DOZE = booleanPreferencesKey("opt_disable_doze")
        val OPT_GPU_TUNING = booleanPreferencesKey("opt_gpu_tuning")
        val OPT_BT_AUDIO = booleanPreferencesKey("opt_bt_audio")
        val OPT_MEMORY_TRIM = booleanPreferencesKey("opt_memory_trim")
        val OPT_DNS = booleanPreferencesKey("opt_dns")
    }

    val preferredRobloxApp: Flow<String> = appContext.dataStore.data.map { it[Keys.PREFERRED_ROBLOX_APP] ?: "global" }
    val customRobloxPackage: Flow<String> = appContext.dataStore.data.map { it[Keys.CUSTOM_ROBLOX_PACKAGE] ?: "" }
    val theme: Flow<String> = appContext.dataStore.data.map { it[Keys.THEME] ?: "dark" }
    val injectionStrategy: Flow<String> = appContext.dataStore.data.map { it[Keys.INJECTION_STRATEGY] ?: "shizuku" }
    val allowManageFFlags: Flow<Boolean> = appContext.dataStore.data.map { it[Keys.ALLOW_MANAGE_FFLAGS] ?: true }
    val lastPreset: Flow<String> = appContext.dataStore.data.map { it[Keys.LAST_PRESET] ?: "" }
    val lastLaunchTs: Flow<String> = appContext.dataStore.data.map { it[Keys.LAST_LAUNCH_TS] ?: "" }

    val optCpuGovernor: Flow<Boolean> = appContext.dataStore.data.map { it[Keys.OPT_CPU_GOVERNOR] ?: false }
    val optKillBg: Flow<Boolean> = appContext.dataStore.data.map { it[Keys.OPT_KILL_BG] ?: false }
    val optClearCache: Flow<Boolean> = appContext.dataStore.data.map { it[Keys.OPT_CLEAR_CACHE] ?: true }
    val optDisableDoze: Flow<Boolean> = appContext.dataStore.data.map { it[Keys.OPT_DISABLE_DOZE] ?: true }
    val optGpuTuning: Flow<Boolean> = appContext.dataStore.data.map { it[Keys.OPT_GPU_TUNING] ?: true }
    val optBtAudio: Flow<Boolean> = appContext.dataStore.data.map { it[Keys.OPT_BT_AUDIO] ?: false }
    val optMemoryTrim: Flow<Boolean> = appContext.dataStore.data.map { it[Keys.OPT_MEMORY_TRIM] ?: true }
    val optDns: Flow<Boolean> = appContext.dataStore.data.map { it[Keys.OPT_DNS] ?: false }

    suspend fun setPreferredRobloxApp(value: String) = appContext.dataStore.edit { it[Keys.PREFERRED_ROBLOX_APP] = value }
    suspend fun setCustomRobloxPackage(value: String) = appContext.dataStore.edit { it[Keys.CUSTOM_ROBLOX_PACKAGE] = value }
    suspend fun setTheme(value: String) = appContext.dataStore.edit { it[Keys.THEME] = value }
    suspend fun setInjectionStrategy(value: String) = appContext.dataStore.edit { it[Keys.INJECTION_STRATEGY] = value }
    suspend fun setAllowManageFFlags(value: Boolean) = appContext.dataStore.edit { it[Keys.ALLOW_MANAGE_FFLAGS] = value }
    suspend fun setLastPreset(value: String) = appContext.dataStore.edit { it[Keys.LAST_PRESET] = value }
    suspend fun setLastLaunchTs(value: String) = appContext.dataStore.edit { it[Keys.LAST_LAUNCH_TS] = value }

    suspend fun setOptCpuGovernor(v: Boolean) = appContext.dataStore.edit { it[Keys.OPT_CPU_GOVERNOR] = v }
    suspend fun setOptKillBg(v: Boolean) = appContext.dataStore.edit { it[Keys.OPT_KILL_BG] = v }
    suspend fun setOptClearCache(v: Boolean) = appContext.dataStore.edit { it[Keys.OPT_CLEAR_CACHE] = v }
    suspend fun setOptDisableDoze(v: Boolean) = appContext.dataStore.edit { it[Keys.OPT_DISABLE_DOZE] = v }
    suspend fun setOptGpuTuning(v: Boolean) = appContext.dataStore.edit { it[Keys.OPT_GPU_TUNING] = v }
    suspend fun setOptBtAudio(v: Boolean) = appContext.dataStore.edit { it[Keys.OPT_BT_AUDIO] = v }
    suspend fun setOptMemoryTrim(v: Boolean) = appContext.dataStore.edit { it[Keys.OPT_MEMORY_TRIM] = v }
    suspend fun setOptDns(v: Boolean) = appContext.dataStore.edit { it[Keys.OPT_DNS] = v }

    val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
}

@Serializable
data class ChevstrapSettingsDto(
    val preferredRobloxApp: String = "global",
    val customRobloxPackage: String = "",
    val theme: String = "dark",
    val injectionStrategy: String = "shizuku",
    val allowManageFFlags: Boolean = true,
    val lastPreset: String = "",
    val lastLaunchTs: String = "",
)
