package com.chipstrap.rbx.fflags.strategies

import android.content.Context
import com.chipstrap.rbx.core.Logger
import com.chipstrap.rbx.data.AppPaths
import java.io.File

/**
 * Root-based injection. Writes ClientAppSettings.json directly into
 * /data/data/<robloxPackage>/files/ClientSettings/.
 *
 * Requires the device to be rooted (su binary present and granted).
 * Most reliable strategy; survives Roblox updates because we always
 * resolve the live data dir via `pm` rather than hard-coding a path.
 */
object RootInjectionStrategy : InjectionStrategy {

    override val id: String = "root"

    override suspend fun isAvailable(context: Context): Boolean {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val ok = p.waitFor() == 0
            ok
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun apply(context: Context, robloxPackage: String): Result<Unit> {
        val src = AppPaths.clientAppSettingsFile
        if (!src.exists()) {
            return Result.failure(IllegalStateException("No ClientAppSettings.json to inject"))
        }
        return try {
            // Resolve the live data dir, don't assume /data/data
            val dirPipe = Runtime.getRuntime()
                .exec(arrayOf("su", "-c", "pm path $robloxPackage"))
            val out = dirPipe.inputStream.bufferedReader().readText().trim()
            Logger.writeLine("RootInjection::apply", "pm path $robloxPackage -> $out")
            val dataDir = "/data/data/$robloxPackage"
            val targetDir = "$dataDir/files/ClientSettings"
            val targetFile = "$targetDir/ClientAppSettings.json"

            val cmds = arrayOf(
                "su", "-c",
                "mkdir -p $targetDir && " +
                    "cp ${src.absolutePath} $targetFile && " +
                    "chown $(stat -c %u $dataDir):$(stat -c %g $dataDir) $targetFile && " +
                    "chmod 660 $targetFile && " +
                    "echo OK"
            )
            val p = Runtime.getRuntime().exec(cmds)
            val err = p.errorStream.bufferedReader().readText()
            val code = p.waitFor()
            if (code == 0) {
                Logger.writeLine("RootInjection::apply", "Injected $targetFile")
                Result.success(Unit)
            } else {
                Logger.writeLine("RootInjection::apply", "su failed code=$code err=$err")
                Result.failure(RuntimeException("su exit $code: $err"))
            }
        } catch (e: Exception) {
            Logger.writeException("RootInjection::apply", e)
            Result.failure(e)
        }
    }
}
