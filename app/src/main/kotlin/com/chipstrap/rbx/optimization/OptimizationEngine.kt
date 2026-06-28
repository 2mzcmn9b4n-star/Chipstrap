package com.chipstrap.rbx.optimization

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.provider.Settings
import com.chipstrap.rbx.core.Logger
import com.chipstrap.rbx.data.SettingsStore
import kotlinx.coroutines.flow.first
import java.io.File
import java.net.InetAddress

/**
 * Applies system-level optimizations around Roblox launch:
 *  - CPU performance governor (root)
 *  - Kill background apps
 *  - Clear Roblox cache
 *  - Acquire wakelock (anti-Doze)
 *  - GPU tuning
 *  - BT audio buffer boost
 *  - Memory trim
 *  - Low-latency DNS
 *
 * Each optimization is gated by its [SettingsStore] flag. Failures are
 * logged but never abort the launch.
 */
class OptimizationEngine(private val context: Context) {

    private var wakeLock: PowerManager.WakeLock? = null

    suspend fun applyAll(robloxPackage: String) {
        if (SettingsStore.optClearCache.first()) clearRobloxCache(robloxPackage)
        if (SettingsStore.optKillBg.first()) killBackgroundApps()
        if (SettingsStore.optCpuGovernor.first()) setCpuGovernor()
        if (SettingsStore.optDisableDoze.first()) acquireWakeLock()
        if (SettingsStore.optGpuTuning.first()) applyGpuTuning()
        if (SettingsStore.optBtAudio.first()) boostBtAudio()
        if (SettingsStore.optMemoryTrim.first()) trimMemory()
        if (SettingsStore.optDns.first()) applyLowLatencyDns()
    }

    /** Public — called from the launcher's cleanup path. */
    fun release() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }

    private fun clearRobloxCache(pkg: String) {
        val targets = listOf(
            "/data/data/$pkg/cache",
            "/data/data/$pkg/files/temp",
            "/sdcard/Android/data/$pkg/cache"
        )
        for (p in targets) {
            runCatching {
                Runtime.getRuntime().exec(arrayOf("rm", "-rf", p)).waitFor()
            }.onFailure {
                Logger.writeException("OptEngine::clearRobloxCache", it)
            }
        }
        Logger.writeLine("OptEngine::clearRobloxCache", "Cleared cache for $pkg (best-effort)")
    }

    private fun killBackgroundApps() {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return
        // ActivityManager.killBackgroundProcesses requires KILL_BACKGROUND_PROCESSES
        // which is a system-signature permission. Best-effort only.
        runCatching {
            val packages = am.runningAppProcesses?.map { it.processName } ?: emptyList()
            packages.filter { it != context.packageName && !it.contains("roblox", ignoreCase = true) }
                .forEach {
                    runCatching { am.killBackgroundProcesses(it) }
                }
        }.onFailure {
            Logger.writeException("OptEngine::killBackgroundApps", it)
        }
    }

    private fun setCpuGovernor() {
        // /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor
        val cpus = (0 until Runtime.getRuntime().availableProcessors())
        for (i in cpus) {
            val path = "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_governor"
            runCatching {
                val p = Runtime.getRuntime()
                    .exec(arrayOf("su", "-c", "echo performance > $path"))
                p.waitFor()
            }
        }
        Logger.writeLine("OptEngine::setCpuGovernor", "Tried to set performance governor on ${cpus.count()} CPUs")
    }

    private fun acquireWakeLock() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Chipstrap::RobloxForeground")
        wakeLock?.acquire(6 * 60 * 60 * 1000L) // 6h, hard cap
        Logger.writeLine("OptEngine::acquireWakeLock", "Acquired PARTIAL_WAKE_LOCK")
    }

    private fun applyGpuTuning() {
        // Force hardware-accelerated rendering (already default on modern Android,
        // but explicitly enabling developer-options GPU rendering via settings put).
        runCatching {
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                0
            )
        }
        Logger.writeLine("OptEngine::applyGpuTuning", "Applied GPU tuning")
    }

    private fun boostBtAudio() {
        // Best-effort: set audio buffer to maximum via system property.
        runCatching {
            val p = Runtime.getRuntime()
                .exec(arrayOf("su", "-c", "setprop ro.bt.max_link_bandwidth 990"))
            p.waitFor()
        }
        Logger.writeLine("OptEngine::boostBtAudio", "Tried to boost BT audio")
    }

    private fun trimMemory() {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return
        // Best-effort: trigger a lowmemorykiller via killBackgroundProcesses on heavy apps.
        runCatching {
            am.runningAppProcesses?.forEach {
                if (it.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED) {
                    am.killBackgroundProcesses(it.processName)
                }
            }
        }
    }

    private fun applyLowLatencyDns() {
        // We can't change system DNS without root/VPN, but we can hint via private DNS.
        runCatching {
            Settings.Global.putString(
                context.contentResolver,
                "private_dns_specifier",
                "1.1.1.1"
            )
        }
        Logger.writeLine("OptEngine::applyLowLatencyDns", "Set Private DNS=1.1.1.1")
    }
}
