package com.chipstrap.rbx.data

import android.content.Context
import java.io.File

/**
 * All on-device paths Chipstrap cares about. Initialized once from [ChipstrapApp].
 */
object AppPaths {

    lateinit var filesDir: File
        private set
    lateinit var cacheDir: File
        private set
    lateinit var logsDir: File
        private set
    lateinit var backupsDir: File
        private set
    lateinit var modificationsDir: File
        private set

    fun init(context: Context) {
        filesDir = context.filesDir
        cacheDir = context.cacheDir
        logsDir = File(filesDir, "Logs").apply { mkdirs() }
        backupsDir = File(filesDir, "Backups").apply { mkdirs() }
        modificationsDir = File(filesDir, "Modifications").apply { mkdirs() }
    }

    /** ClientAppSettings.json kept locally, mirrors BloxStrap format. */
    val clientAppSettingsFile: File
        get() = File(File(modificationsDir, "ClientSettings"), "ClientAppSettings.json")
}
