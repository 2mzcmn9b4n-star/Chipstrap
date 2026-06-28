package com.chipstrap.rbx.fflags.strategies

import android.content.Context
import com.chipstrap.rbx.core.Logger
import kotlinx.coroutines.flow.first
import com.chipstrap.rbx.data.SettingsStore

/**
 * Picks the best [InjectionStrategy] at runtime based on user preference
 * and on-device availability. Falls back through a priority chain.
 */
object StrategyResolver {

    val all: List<InjectionStrategy> = listOf(
        ShizukuInjectionStrategy,
        RootInjectionStrategy,
        VirtualSpaceInjectionStrategy,
        LocalProfileStrategy
    )

    fun byId(id: String): InjectionStrategy = all.firstOrNull { it.id == id } ?: LocalProfileStrategy

    /**
     * Resolve the user's preferred strategy. If it's unavailable, walk down
     * the priority chain and return the first available one. Always returns
     * [LocalProfileStrategy] as a last resort.
     */
    suspend fun resolve(context: Context): Pair<InjectionStrategy, Boolean> {
        val preferredId = SettingsStore.injectionStrategy.first()
        val preferred = byId(preferredId)
        if (preferred.isAvailable(context)) return preferred to true

        for (s in all) {
            if (s.isAvailable(context)) {
                Logger.writeLine("StrategyResolver::resolve", "Preferred '$preferredId' unavailable, falling back to ${s.id}")
                return s to false
            }
        }
        return LocalProfileStrategy to false
    }
}
