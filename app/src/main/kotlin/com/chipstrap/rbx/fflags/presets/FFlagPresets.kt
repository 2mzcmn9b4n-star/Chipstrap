package com.chipstrap.rbx.fflags.presets

import com.chipstrap.rbx.fflags.repository.FFlagEntry

/**
 * A named bundle of FFlags the user can apply with one tap.
 *
 * Each preset sets an explicit list of Roblox FastFlags that are known
 * to take effect on the Android client (despite Roblox's recent clampdown
 * on external FFlag reads — Chipstrap's injector strategies work around it).
 */
data class FFlagPreset(
    val id: String,
    val titleRes: Int,
    val descRes: Int,
    val flags: List<FFlagEntry>
)

object FFlagPresets {

    /** Common flags every preset explicitly disables, because Roblox turned
     *  them on by default and they're pure overhead on Android. */
    private val alwaysOff = listOf(
        FFlagEntry("FFlagDebugGraphicsPreferVulkan", "False"),
        FFlagEntry("FFlagDisablePostFx", "True"),
        FFlagEntry("FFlagHandleAltEnterFullscreen", "False"),
        FFlagEntry("FFlagDebugForceFutureIsBright", "False"),
        FFlagEntry("FFlagFastGPULightCulling", "True"),
        // Workaround: Roblox 2.650+ stopped honoring ClientAppSettings.json
        // unless DFIntTaskSchedulerTargetFps is also present. Force-include.
        FFlagEntry("FIntTaskSchedulerTargetFps", "60")
    )

    private val ultraFps = listOf(
        FFlagEntry("FFlagDisablePostFx", "True"),
        FFlagEntry("FFlagDebugGraphicsDisableDirect3D11", "True"),
        FFlagEntry("FIntRenderShadowIntensity", "0"),
        FFlagEntry("FIntRenderShadowMapBias", "0"),
        FFlagEntry("FFlagDisableShadows", "True"),
        FFlagEntry("FIntDebugForceMSAA", "0"),
        FFlagEntry("FIntTerrainArraySliceSize", "0"),
        FFlagEntry("FIntRenderShadowIntensity", "0"),
        FFlagEntry("FIntRenderLocalLightUpdatesMin", "0"),
        FFlagEntry("FIntRenderLocalLightUpdatesMax", "1"),
        FFlagEntry("FFlagDisableDynamicSky", "True"),
        FFlagEntry("FFlagCoreScriptTypeDisable", "True"),
        FFlagEntry("FIntTaskSchedulerTargetFps", "120"),
        FFlagEntry("FFlagDebugForceFutureIsBright", "False"),
        FFlagEntry("FFlagRenderPlatformIndependenceEnabled", "False"),
        FFlagEntry("FIntTextureQuality", "0"),
        FFlagEntry("FFlagFastGPULightCulling", "True"),
        FFlagEntry("DFIntTextureQualityOverride", "0"),
        FFlagEntry("FIntRenderGrassDensity", "0"),
        FFlagEntry("FFlagDisableBloom", "True"),
        FFlagEntry("FFlagDisableDepthOfField", "True"),
        FFlagEntry("FFlagDisableCAT", "True")
    )

    private val balanced = listOf(
        FFlagEntry("FFlagDebugGraphicsPreferVulkan", "True"),
        FFlagEntry("FIntDebugForceMSAA", "2"),
        FFlagEntry("FIntTextureQuality", "2"),
        FFlagEntry("DFIntTextureQualityOverride", "2"),
        FFlagEntry("FIntTaskSchedulerTargetFps", "60"),
        FFlagEntry("FFlagDisablePostFx", "False"),
        FFlagEntry("FFlagDisableShadows", "False"),
        FFlagEntry("FIntRenderShadowIntensity", "1"),
        FFlagEntry("FIntRenderGrassDensity", "1"),
        FFlagEntry("FFlagDisableBloom", "False"),
        FFlagEntry("FFlagDisableDepthOfField", "True"),
        FFlagEntry("FIntRenderLocalLightUpdatesMin", "1"),
        FFlagEntry("FIntRenderLocalLightUpdatesMax", "4"),
        FFlagEntry("FFlagFastGPULightCulling", "True")
    )

    private val batterySaver = listOf(
        FFlagEntry("FIntTaskSchedulerTargetFps", "30"),
        FFlagEntry("FIntRenderShadowIntensity", "0"),
        FFlagEntry("FFlagDisableShadows", "True"),
        FFlagEntry("FFlagDisablePostFx", "True"),
        FFlagEntry("FFlagDisableBloom", "True"),
        FFlagEntry("FFlagDisableDepthOfField", "True"),
        FFlagEntry("FIntDebugForceMSAA", "0"),
        FFlagEntry("FIntTextureQuality", "0"),
        FFlagEntry("DFIntTextureQualityOverride", "0"),
        FFlagEntry("FIntRenderGrassDensity", "0"),
        FFlagEntry("FIntRenderLocalLightUpdatesMin", "0"),
        FFlagEntry("FIntRenderLocalLightUpdatesMax", "1"),
        FFlagEntry("FFlagRenderPlatformIndependenceEnabled", "False"),
        FFlagEntry("FIntTerrainArraySliceSize", "0"),
        FFlagEntry("FIntDebugForceMSAA", "0")
    )

    private val highQuality = listOf(
        FFlagEntry("FFlagDebugGraphicsPreferVulkan", "True"),
        FFlagEntry("FIntDebugForceMSAA", "4"),
        FFlagEntry("FIntTextureQuality", "3"),
        FFlagEntry("DFIntTextureQualityOverride", "3"),
        FFlagEntry("FIntTaskSchedulerTargetFps", "60"),
        FFlagEntry("FFlagDisablePostFx", "False"),
        FFlagEntry("FFlagDisableShadows", "False"),
        FFlagEntry("FIntRenderShadowIntensity", "1"),
        FFlagEntry("FFlagDisableBloom", "False"),
        FFlagEntry("FFlagDisableDepthOfField", "False"),
        FFlagEntry("FIntRenderGrassDensity", "2"),
        FFlagEntry("FIntRenderLocalLightUpdatesMin", "2"),
        FFlagEntry("FIntRenderLocalLightUpdatesMax", "8"),
        FFlagEntry("FFlagFastGPULightCulling", "True")
    )

    private val lowEnd = listOf(
        FFlagEntry("FIntTaskSchedulerTargetFps", "60"),
        FFlagEntry("FFlagDisableShadows", "True"),
        FFlagEntry("FIntRenderShadowIntensity", "0"),
        FFlagEntry("FFlagDisablePostFx", "True"),
        FFlagEntry("FFlagDisableBloom", "True"),
        FFlagEntry("FFlagDisableDepthOfField", "True"),
        FFlagEntry("FIntDebugForceMSAA", "0"),
        FFlagEntry("FIntTextureQuality", "0"),
        FFlagEntry("DFIntTextureQualityOverride", "0"),
        FFlagEntry("FIntRenderGrassDensity", "0"),
        FFlagEntry("FIntRenderLocalLightUpdatesMin", "0"),
        FFlagEntry("FIntRenderLocalLightUpdatesMax", "2"),
        FFlagEntry("FFlagRenderPlatformIndependenceEnabled", "False"),
        FFlagEntry("FFlagDisableDynamicSky", "True"),
        FFlagEntry("FFlagDebugForceFutureIsBright", "False"),
        // Less aggressive asset streaming to avoid OOM on low-RAM devices.
        FFlagEntry("DFIntAssetStreamingStatusReportEnabled", "0"),
        FFlagEntry("FFlagCoreScriptTypeDisable", "True")
    )

    private val competitive = listOf(
        FFlagEntry("FIntTaskSchedulerTargetFps", "1000"), // uncapped
        FFlagEntry("FFlagDisableShadows", "True"),
        FFlagEntry("FFlagDisablePostFx", "True"),
        FFlagEntry("FFlagDisableBloom", "True"),
        FFlagEntry("FFlagDisableDepthOfField", "True"),
        FFlagEntry("FIntRenderShadowIntensity", "0"),
        FFlagEntry("FIntDebugForceMSAA", "0"),
        FFlagEntry("FIntTextureQuality", "0"),
        FFlagEntry("DFIntTextureQualityOverride", "0"),
        FFlagEntry("FIntRenderGrassDensity", "0"),
        FFlagEntry("FFlagRenderPlatformIndependenceEnabled", "False"),
        FFlagEntry("FFlagDebugForceFutureIsBright", "False"),
        FFlagEntry("FFlagFastGPULightCulling", "True"),
        // Lower input latency
        FFlagEntry("FIntRenderLocalLightUpdatesMin", "0"),
        FFlagEntry("FIntRenderLocalLightUpdatesMax", "1"),
        FFlagEntry("DFIntTaskSchedulerTargetFps", "1000")
    )

    val all: List<FFlagPreset> = listOf(
        FFlagPreset("ultra_fps", com.chipstrap.rbx.R.string.preset_ultra_fps, com.chipstrap.rbx.R.string.preset_ultra_fps_desc, ultraFps + alwaysOff),
        FFlagPreset("balanced", com.chipstrap.rbx.R.string.preset_balanced, com.chipstrap.rbx.R.string.preset_balanced_desc, balanced + alwaysOff),
        FFlagPreset("battery_saver", com.chipstrap.rbx.R.string.preset_battery_saver, com.chipstrap.rbx.R.string.preset_battery_saver_desc, batterySaver + alwaysOff),
        FFlagPreset("high_quality", com.chipstrap.rbx.R.string.preset_high_quality, com.chipstrap.rbx.R.string.preset_high_quality_desc, highQuality + alwaysOff),
        FFlagPreset("low_end", com.chipstrap.rbx.R.string.preset_low_end, com.chipstrap.rbx.R.string.preset_low_end_desc, lowEnd + alwaysOff),
        FFlagPreset("competitive", com.chipstrap.rbx.R.string.preset_competitive, com.chipstrap.rbx.R.string.preset_competitive_desc, competitive + alwaysOff)
    )

    fun byId(id: String): FFlagPreset? = all.firstOrNull { it.id == id }
}
