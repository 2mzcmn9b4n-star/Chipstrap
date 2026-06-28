package com.chipstrap.rbx.fflags.strategies

import android.content.Context
import com.chipstrap.rbx.core.Logger
import com.chipstrap.rbx.data.AppPaths
import java.io.File

/**
 * Virtual-space strategy: writes the JSON into a well-known directory
 * that parallel-space apps expose for their virtualized Roblox process.
 *
 * Multiple virtual space apps exist (Parallel Space, DualSpace, Multiple
 * Accounts). We try the most common exposed paths. The user is expected
 * to launch Roblox from within the virtual space, not from the system
 * launcher, for this to take effect.
 */
object VirtualSpaceInjectionStrategy : InjectionStrategy {

    override val id: String = "virtual"

    private val candidateRoots = listOf(
        "/sdcard/Android/data/com.excelliance.dualaid",
        "/sdcard/Android/data/com.lbe.parallel.intl",
        "/sdcard/Android/data/com.ludashi.dualspace",
        "/sdcard/ParallelSpace",
        "/sdcard/DualSpace"
    )

    override suspend fun isAvailable(context: Context): Boolean {
        return candidateRoots.any { File(it).exists() }
    }

    override suspend fun apply(context: Context, robloxPackage: String): Result<Unit> {
        val src = AppPaths.clientAppSettingsFile
        if (!src.exists()) {
            return Result.failure(IllegalStateException("No ClientAppSettings.json to inject"))
        }
        val tried = mutableListOf<String>()
        for (root in candidateRoots) {
            val targetDir = File(root, "roblox/$robloxPackage/files/ClientSettings")
            try {
                targetDir.mkdirs()
                val targetFile = File(targetDir, "ClientAppSettings.json")
                src.copyTo(targetFile, overwrite = true)
                Logger.writeLine("VirtualSpaceInjection::apply", "Injected to ${targetFile.absolutePath}")
                return Result.success(Unit)
            } catch (e: Exception) {
                tried += "${root}: ${e.message}"
            }
        }
        return Result.failure(RuntimeException("No virtual space root found. Tried: $tried"))
    }
}
