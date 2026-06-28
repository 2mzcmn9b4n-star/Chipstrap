package com.chipstrap.rbx.fflags.repository

import com.chipstrap.rbx.core.Logger
import com.chipstrap.rbx.data.AppPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * In-memory representation of the ClientAppSettings.json we want to inject into Roblox.
 *
 * File format (mirrors BloxStrap / Roblox's expected schema):
 * ```
 * {
 *   "FFlags": {
 *      "FFlagDebugGraphicsDisableDirect3D11": "False",
 *      "FIntRenderShadowIntensity": "0"
 *   }
 * }
 * ```
 * All values are stored as strings — Roblox's FastFlag manager parses them on the client side.
 */
@Serializable
data class FFlagEntry(val key: String, val value: String)

class FFlagRepository {

    private val _flags = MutableStateFlow<List<FFlagEntry>>(emptyList())
    val flags: StateFlow<List<FFlagEntry>> = _flags.asStateFlow()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun load(): List<FFlagEntry> = withContext(Dispatchers.IO) {
        val file = AppPaths.clientAppSettingsFile
        val result: List<FFlagEntry> = try {
            if (file.exists()) {
                val raw = file.readText()
                if (raw.isNotBlank()) {
                    val obj = json.parseToJsonElement(raw).jsonObject
                    val fflags = obj["FFlags"]?.jsonObject ?: JsonObject(emptyMap())
                    fflags.entries.map { FFlagEntry(it.key, it.value.jsonPrimitive.contentOrNull ?: "") }
                        .sortedBy { it.key }
                } else emptyList()
            } else emptyList()
        } catch (e: Exception) {
            Logger.writeException("FFlagRepository::load", e)
            emptyList()
        }
        _flags.value = result
        result
    }

    suspend fun save(entries: List<FFlagEntry>) = withContext(Dispatchers.IO) {
        val obj = buildJsonObject {
            put("FFlags", buildJsonObject {
                entries.sortedBy { it.key }.forEach {
                    put(it.key, JsonPrimitive(it.value))
                }
            })
        }
        val file = AppPaths.clientAppSettingsFile
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(JsonObject.serializer(), obj))
        _flags.value = entries.sortedBy { it.key }
        Logger.writeLine("FFlagRepository::save", "Saved ${entries.size} FFlags to ${file.absolutePath}")
    }

    suspend fun upsert(key: String, value: String) {
        val current = _flags.value.toMutableList()
        val idx = current.indexOfFirst { it.key.equals(key, ignoreCase = true) }
        if (idx >= 0) current[idx] = FFlagEntry(key, value) else current.add(FFlagEntry(key, value))
        save(current)
    }

    suspend fun delete(key: String) {
        val current = _flags.value.filterNot { it.key.equals(key, ignoreCase = true) }
        save(current)
    }

    suspend fun deleteAll() = save(emptyList())

    suspend fun importJson(raw: String, replace: Boolean): Int {
        val parsed = json.parseToJsonElement(raw).jsonObject
        val fflags = parsed["FFlags"]?.jsonObject ?: parsed // tolerate a bare map too
        val imported = fflags.entries.map { FFlagEntry(it.key, it.value.jsonPrimitive.content) }
        val merged = if (replace) {
            val byKey = _flags.value.associateBy { it.key.lowercase() }.toMutableMap()
            imported.forEach { byKey[it.key.lowercase()] = it }
            byKey.values.toList()
        } else {
            val existing = _flags.value.associateBy { it.key.lowercase() }
            imported.filter { it.key.lowercase() !in existing } + _flags.value
        }
        save(merged)
        return imported.size
    }

    fun exportJson(entries: List<FFlagEntry> = _flags.value): String {
        val obj = buildJsonObject {
            put("FFlags", buildJsonObject {
                entries.sortedBy { it.key }.forEach {
                    put(it.key, JsonPrimitive(it.value))
                }
            })
        }
        return json.encodeToString(JsonObject.serializer(), obj)
    }

    suspend fun backup(name: String) = withContext(Dispatchers.IO) {
        val src = AppPaths.clientAppSettingsFile
        if (!src.exists()) return@withContext
        val dst = File(AppPaths.backupsDir, "$name.json")
        src.copyTo(dst, overwrite = true)
        Logger.writeLine("FFlagRepository::backup", "Backup saved: ${dst.absolutePath}")
    }

    suspend fun restore(name: String): Boolean = withContext(Dispatchers.IO) {
        val src = File(AppPaths.backupsDir, "$name.json")
        if (!src.exists()) return@withContext false
        val dst = AppPaths.clientAppSettingsFile
        dst.parentFile?.mkdirs()
        src.copyTo(dst, overwrite = true)
        load()
        true
    }

    fun listBackups(): List<String> =
        AppPaths.backupsDir.listFiles { f -> f.isFile && f.name.endsWith(".json") }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?: emptyList()

    fun count(): Int = _flags.value.size
}
