package com.chipstrap.rbx

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.chipstrap.rbx.core.Logger
import com.chipstrap.rbx.data.AppPaths
import com.chipstrap.rbx.data.SettingsStore

/**
 * Application entrypoint. Sets up paths, logger, notification channels.
 */
class ChipstrapApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppPaths.init(this)
        Logger.init(AppPaths.logsDir)
        SettingsStore.init(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_LAUNCHER,
                getString(R.string.notif_channel_launcher),
                NotificationManager.IMPORTANCE_LOW
            )
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SERVER,
                getString(R.string.notif_channel_server),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    companion object {
        const val CHANNEL_LAUNCHER = "chipstrap.launcher"
        const val CHANNEL_SERVER = "chipstrap.server"

        @Volatile lateinit var instance: ChipstrapApp
            private set
    }
}
