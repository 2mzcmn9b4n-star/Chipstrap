package com.chipstrap.rbx.fflags.strategies

import android.content.Context
import com.chipstrap.rbx.core.Logger
import com.chipstrap.rbx.data.AppPaths

/**
 * No-op fallback. Always "succeeds" by keeping the local ClientAppSettings.json
 * in place so the user can export it, but does not actually inject anything.
 *
 * This is what the user gets if they have no Shizuku/root/virtual-space setup.
 * The UI shows a clear warning that FFlags won't actually take effect inside
 * Roblox until one of the other strategies is enabled.
 */
object LocalProfileStrategy : InjectionStrategy {

    override val id: String = "local"

    override suspend fun isAvailable(context: Context): Boolean = true

    override suspend fun apply(context: Context, robloxPackage: String): Result<Unit> {
        Logger.writeLine("LocalProfile::apply", "Local profile only — JSON kept at ${AppPaths.clientAppSettingsFile}")
        return Result.success(Unit)
    }
}
