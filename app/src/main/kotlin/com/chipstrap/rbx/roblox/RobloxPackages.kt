package com.chipstrap.rbx.roblox

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.first
import com.chipstrap.rbx.data.SettingsStore

/**
 * Helpers to locate the Roblox package on the device.
 * Supports the global client, the VNG (Vietnam) client, and a custom package
 * (mirrors Chevstrap's behaviour, with cleaner Kotlin).
 */
object RobloxPackages {

    const val GLOBAL = "com.roblox.client"
    const val VNG = "com.roblox.client.vnggames"

    suspend fun resolvePreferred(context: Context): String {
        val pref = SettingsStore.preferredRobloxApp.first()
        return when (pref) {
            "global" -> GLOBAL
            "vng" -> VNG
            "custom" -> SettingsStore.customRobloxPackage.first().ifBlank { GLOBAL }
            else -> GLOBAL
        }
    }

    fun isInstalled(context: Context, pkg: String): Boolean = try {
        context.packageManager.getPackageInfo(pkg, 0); true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    fun anyInstalled(context: Context): Boolean =
        isInstalled(context, GLOBAL) || isInstalled(context, VNG)

    fun versionName(context: Context, pkg: String): String? = try {
        context.packageManager.getPackageInfo(pkg, 0).versionName
    } catch (_: Throwable) { null }

    fun isDebugBuild(context: Context, pkg: String): Boolean = try {
        val ai: ApplicationInfo = context.packageManager.getApplicationInfo(pkg, 0)
        (ai.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    } catch (_: Throwable) { false }

    /** Path of the Roblox data directory (best-effort). */
    fun dataDir(context: Context, pkg: String): String? = try {
        context.packageManager.getApplicationInfo(pkg, 0).dataDir
    } catch (_: Throwable) { null }
}
