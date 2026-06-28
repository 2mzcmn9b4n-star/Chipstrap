package com.chipstrap.rbx.roblox

import android.content.Context
import android.content.Intent
import com.chipstrap.rbx.core.Logger
import com.chipstrap.rbx.data.AppPaths
import com.chipstrap.rbx.data.SettingsStore
import com.chipstrap.rbx.fflags.repository.FFlagRepository
import com.chipstrap.rbx.fflags.strategies.StrategyResolver
import com.chipstrap.rbx.optimization.OptimizationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Result of a launch attempt.
 */
sealed class LaunchResult {
    data object Success : LaunchResult()
    data class Failure(val message: String, val cause: Throwable? = null) : LaunchResult()
    data object NotInstalled : LaunchResult()
}

/**
 * The main launch pipeline:
 *
 *   1. Resolve preferred Roblox package.
 *   2. Make sure it's installed.
 *   3. Load the FFlag repository from disk.
 *   4. Resolve the best injection strategy.
 *   5. Apply optimizations (CPU governor, kill bg, clear cache, wakelock, DNS).
 *   6. Push ClientAppSettings.json into Roblox's data dir.
 *   7. Send the launch intent.
 *   8. Record the launch timestamp.
 */
class RobloxLauncher(
    private val context: Context,
    private val fflags: FFlagRepository = FFlagRepository(),
    private val optimizations: OptimizationEngine = OptimizationEngine(context)
) {

    suspend fun launch(applyChanges: Boolean): LaunchResult = withContext(Dispatchers.IO) {
        Logger.writeLine("RobloxLauncher::launch", "applyChanges=$applyChanges")
        val pkg = RobloxPackages.resolvePreferred(context)
        if (!RobloxPackages.isInstalled(context, pkg)) {
            Logger.writeLine("RobloxLauncher::launch", "Roblox not installed: $pkg")
            return@withContext LaunchResult.NotInstalled
        }

        // Make sure the local FFlag store is loaded.
        fflags.load()

        if (applyChanges) {
            // 1. Apply optimizations BEFORE injecting (clear cache, kill bg, etc.)
            optimizations.applyAll(pkg)

            // 2. Inject FFlags if user has opted in.
            val allow = SettingsStore.allowManageFFlags.first()
            if (allow && fflags.count() > 0) {
                val (strategy, isPreferred) = StrategyResolver.resolve(context)
                Logger.writeLine("RobloxLauncher::launch", "Using strategy: ${strategy.id} (preferred=$isPreferred)")
                val result = strategy.apply(context, pkg)
                if (result.isFailure) {
                    val e = result.exceptionOrNull()
                    Logger.writeException("RobloxLauncher::launch", e ?: RuntimeException("unknown"))
                    // Don't abort launch — Roblox will still run, just without our FFlags.
                }
            } else {
                Logger.writeLine("RobloxLauncher::launch", "Skipping FFlag injection (allow=$allow, count=${fflags.count()})")
            }
        }

        // 3. Launch.
        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
        if (launchIntent == null) {
            return@withContext LaunchResult.Failure("No launch intent for $pkg")
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(launchIntent)
        } catch (e: Exception) {
            return@withContext LaunchResult.Failure("Failed to start activity: ${e.message}", e)
        }

        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        SettingsStore.setLastLaunchTs(ts)
        Logger.writeLine("RobloxLauncher::launch", "Launched $pkg at $ts")
        LaunchResult.Success
    }
}
