package com.chipstrap.rbx.fflags.strategies

import android.content.Context
import com.chipstrap.rbx.data.AppPaths

/**
 * One way to push [AppPaths.clientAppSettingsFile] into Roblox's data directory.
 *
 * Background: starting with Roblox Android client 2.650.x, the client stopped
 * reading ClientAppSettings.json from any path that wasn't its own private
 * data dir, and it verifies a checksum on launch. The classical Chevstrap
 * approach of just dropping the file into a sibling directory stopped working.
 *
 * Chipstrap works around this with multiple strategies, ordered by preference:
 *
 *   1. Shizuku — write through RikkaX Shizuku API into Roblox's data dir.
 *      No root needed; survives Roblox updates; only requires Shizuku to be
 *      running on the device.
 *   2. Root — write directly via `su` if the device is rooted.
 *   3. Virtual space — Roblox runs inside a parallel-space container that
 *      shares its data dir; Chipstrap writes there.
 *   4. Local profile — fallback: cannot inject, but still store the JSON so
 *      it can be exported or used by an external tool.
 *
 * Strategies self-report availability so the launcher can pick the best one
 * at runtime.
 */
interface InjectionStrategy {

    /** Stable id, stored in [com.chipstrap.rbx.data.SettingsStore.Keys.INJECTION_STRATEGY]. */
    val id: String

    /** True if this strategy is usable on the current device right now. */
    suspend fun isAvailable(context: Context): Boolean

    /** Push the local ClientAppSettings.json into Roblox's data dir. */
    suspend fun apply(context: Context, robloxPackage: String): Result<Unit>

    /** Clean up the injected file (optional — does nothing by default). */
    suspend fun cleanup(context: Context, robloxPackage: String) = Result.success(Unit)
}
