package com.chipstrap.rbx.roblox

import android.content.Context
import android.content.Intent
import com.chipstrap.rbx.core.Logger

/**
 * Cleans Roblox cache directories (best-effort, root/virtual-space only).
 * Falls back to no-op silently.
 */
object RobloxCacheCleaner {

    fun clearIfPossible(context: Context, pkg: String) {
        val targets = listOf(
            "/data/data/$pkg/cache",
            "/data/data/$pkg/files/temp",
            "/data/data/$pkg/files/Roblox/appData/cache",
            "/sdcard/Android/data/$pkg/cache",
            "/sdcard/Android/data/$pkg/files/Roblox/appData/cache"
        )
        for (path in targets) {
            try {
                val p = Runtime.getRuntime().exec(arrayOf("rm", "-rf", path))
                val code = p.waitFor()
                Logger.writeLine("RobloxCacheCleaner::clear", "rm -rf $path -> $code")
            } catch (e: Exception) {
                Logger.writeException("RobloxCacheCleaner::clear", e)
            }
        }
    }
}
