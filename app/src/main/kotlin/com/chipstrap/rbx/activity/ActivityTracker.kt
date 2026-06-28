package com.chipstrap.rbx.activity

import com.chipstrap.rbx.core.Logger
import com.chipstrap.rbx.data.AppPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tracks Roblox play sessions, BloxStrap-style.
 *
 * We can't read Roblox's internal state without IPC, so we use a heuristic:
 * the foreground service [com.chipstrap.rbx.service.LauncherForegroundService]
 * starts the session timer when the user taps "Launch" and stops it when
 * Roblox leaves the foreground (detected via Accessibility or usage-stats
 * polling — kept minimal here).
 */
@Serializable
data class ExperienceEntry(
    val placeId: String,
    val placeName: String,
    val startedAt: String,
    val durationMs: Long
)

@Serializable
data class ActivitySnapshot(
    val sessionStart: String,
    val totalMs: Long,
    val experiences: List<ExperienceEntry>
)

class ActivityTracker {

    private val _state = MutableStateFlow(
        ActivitySnapshot("", 0L, emptyList())
    )
    val state: StateFlow<ActivitySnapshot> = _state.asStateFlow()

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private val file: File get() = File(AppPaths.filesDir, "activity.json")

    suspend fun load() = withContext(Dispatchers.IO) {
        runCatching {
            if (file.exists()) {
                _state.value = json.decodeFromString(ActivitySnapshot.serializer(), file.readText())
            }
        }.onFailure { Logger.writeException("ActivityTracker::load", it) }
    }

    suspend fun startSession() = withContext(Dispatchers.IO) {
        val now = fmt.format(Date())
        _state.value = _state.value.copy(sessionStart = now)
        persist()
    }

    suspend fun addExperience(placeId: String, placeName: String, durationMs: Long) = withContext(Dispatchers.IO) {
        val now = fmt.format(Date())
        val entry = ExperienceEntry(placeId, placeName, now, durationMs)
        val updated = _state.value.copy(
            totalMs = _state.value.totalMs + durationMs,
            experiences = (_state.value.experiences + entry).takeLast(50)
        )
        _state.value = updated
        persist()
    }

    suspend fun endSession(durationMs: Long) = withContext(Dispatchers.IO) {
        _state.value = _state.value.copy(totalMs = _state.value.totalMs + durationMs)
        persist()
    }

    private fun persist() {
        runCatching {
            file.writeText(json.encodeToString(ActivitySnapshot.serializer(), _state.value))
        }.onFailure { Logger.writeException("ActivityTracker::persist", it) }
    }
}
