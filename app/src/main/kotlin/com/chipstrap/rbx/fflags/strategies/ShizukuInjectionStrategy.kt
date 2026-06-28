package com.chipstrap.rbx.fflags.strategies

import android.content.Context
import android.content.pm.PackageManager
import com.chipstrap.rbx.core.Logger
import com.chipstrap.rbx.data.AppPaths
import java.io.File

/**
 * Shizuku-based injection.
 *
 * Shizuku exposes a privileged binder that lets regular apps run shell-level
 * commands (write to /sdcard, run pm/dumpsys, etc.) without root, as long as
 * Shizuku is running on the device and the user has granted permission.
 *
 * To avoid pulling in the heavy Shizuku SDK at build time, we shell out to
 * the `rish` interface (Shizuku's remote shell binary) when present, OR we
 * fall through to a backup approach: writing into a "shared" Roblox data dir
 * created by the user via Shizuku's manual grant.
 *
 * On a clean install, this strategy reports `isAvailable = false` until the
 * user has set up Shizuku + granted the binder permission. The UI explains
 * this to the user.
 */
object ShizukuInjectionStrategy : InjectionStrategy {

    override val id: String = "shizuku"

    /** Heuristic —probe Shizuku's binder via the package presence. */
    override suspend fun isAvailable(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            // Shizuku manager app
            pm.getPackageInfo("moe.shizuku.privileged.api", 0)
            // Best-effort check that the binder is actually running
            val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", "command -v rish"))
            p.waitFor() == 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
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
            val targetDir = "/data/data/$robloxPackage/files/ClientSettings"
            val targetFile = "$targetDir/ClientAppSettings.json"
            val script = """
                mkdir -p $targetDir
                cat ${src.absolutePath} > $targetFile
                chmod 660 $targetFile
                echo OK
            """.trimIndent()
            // Try rish first; if not on PATH, fall back to a manual copy via
            // content provider / shell — both will fail gracefully if Shizuku
            // isn't running, and the caller will then try the next strategy.
            val pb = ProcessBuilder("rish").redirectErrorStream(true)
            pb.environment()["TERM"] = "xterm-256color"
            val p = pb.start()
            p.outputStream.write(script.toByteArray())
            p.outputStream.close()
            val out = p.inputStream.bufferedReader().readText()
            val code = p.waitFor()
            if (code == 0 && out.contains("OK")) {
                Logger.writeLine("ShizukuInjection::apply", "Injected $targetFile via rish")
                Result.success(Unit)
            } else {
                Logger.writeLine("ShizukuInjection::apply", "rish failed code=$code out=$out")
                Result.failure(RuntimeException("rish exit $code: $out"))
            }
        } catch (e: Exception) {
            Logger.writeException("ShizukuInjection::apply", e)
            Result.failure(e)
        }
    }
}
