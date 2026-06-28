package com.chipstrap.rbx.server

import com.chipstrap.rbx.core.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.util.concurrent.TimeUnit

/**
 * Resolves information about the Roblox server the user is currently connected to,
 * BloxStrap-style. We can't read the live JobID from the official Android client
 * (no public API), so we expose a manual "look up a JobID" UI flow plus a polling
 * mode that watches /sdcard/Android/data/com.roblox.client/files/logs for the
 * latest server info line that Roblox still writes there.
 */
class ServerInfoProvider {

    private val http = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class ServerInfo(
        val jobId: String,
        val universeId: String? = null,
        val placeId: String? = null,
        val host: String? = null,
        val location: String? = null,
        val pingMs: Int? = null,
        val playerCount: Int? = null
    )

    @Serializable
    data class IpInfoResponse(val city: String? = null, val country: String? = null, val loc: String? = null)

    /** Resolve a server's geographic location from its public IP. */
    suspend fun lookupLocation(ip: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val req = Request.Builder().url("https://ipinfo.io/$ip/json").build()
            http.newCall(req).execute().use { r ->
                if (!r.isSuccessful) return@use null
                val body = r.body?.string() ?: return@use null
                val info = json.decodeFromString(IpInfoResponse.serializer(), body)
                listOfNotNull(info.city, info.country).joinToString(", ").ifBlank { null }
            }
        }.getOrElse {
            Logger.writeException("ServerInfoProvider::lookupLocation", it); null
        }
    }

    /** Quick ICMP-ish ping via InetAddress.isReachable (best-effort). */
    suspend fun ping(host: String, timeoutMs: Int = 1500): Int? = withContext(Dispatchers.IO) {
        runCatching {
            val start = System.currentTimeMillis()
            val reachable = InetAddress.getByName(host).isReachable(timeoutMs)
            if (reachable) (System.currentTimeMillis() - start).toInt() else null
        }.getOrNull()
    }

    /**
     * Poll Roblox's log directory for the most recent JobID/server line. The Roblox
     * Android client still writes a "Joined server jobId=… placeId=… universeId=…"
     * entry to its log directory on most builds; we tail it.
     */
    suspend fun pollFromRobloxLogs(robloxPkg: String): ServerInfo? = withContext(Dispatchers.IO) {
        val candidates = listOf(
            "/sdcard/Android/data/$robloxPkg/files/logs/latest.log",
            "/data/data/$robloxPkg/files/logs/latest.log",
            "/sdcard/Android/data/$robloxPkg/files/Roblox/logs/latest.log"
        )
        for (path in candidates) {
            val f = java.io.File(path)
            if (!f.exists()) continue
            runCatching {
                val tail = f.readLines().takeLast(200)
                var job: String? = null
                var place: String? = null
                var universe: String? = null
                var host: String? = null
                for (line in tail) {
                    if (job == null && line.contains("jobId=")) {
                        job = Regex("jobId=([0-9a-fA-F\\-]+)").find(line)?.groupValues?.get(1)
                    }
                    if (place == null && line.contains("placeId=")) {
                        place = Regex("placeId=(\\d+)").find(line)?.groupValues?.get(1)
                    }
                    if (universe == null && line.contains("universeId=")) {
                        universe = Regex("universeId=(\\d+)").find(line)?.groupValues?.get(1)
                    }
                    if (host == null && line.contains("host=")) {
                        host = Regex("host=([0-9a-zA-Z\\.\\-]+)").find(line)?.groupValues?.get(1)
                    }
                }
                if (job != null) {
                    return@withContext ServerInfo(
                        jobId = job!!,
                        universeId = universe,
                        placeId = place,
                        host = host
                    )
                }
            }
        }
        null
    }
}
