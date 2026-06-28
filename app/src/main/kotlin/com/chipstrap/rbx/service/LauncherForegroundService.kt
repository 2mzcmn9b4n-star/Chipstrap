package com.chipstrap.rbx.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.chipstrap.rbx.ChipstrapApp
import com.chipstrap.rbx.MainActivity
import com.chipstrap.rbx.R
import com.chipstrap.rbx.activity.ActivityTracker
import com.chipstrap.rbx.core.Logger
import com.chipstrap.rbx.data.SettingsStore
import com.chipstrap.rbx.optimization.OptimizationEngine
import com.chipstrap.rbx.roblox.RobloxLauncher
import com.chipstrap.rbx.roblox.LaunchResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Foreground service that owns the Roblox launch pipeline. Keeping it in a
 * foreground service ensures:
 *   - We don't get killed mid-apply when the user background us
 *   - The wakelock (anti-Doze) stays valid for the whole session
 *   - The activity tracker has a stable lifetime
 */
class LauncherForegroundService : LifecycleService() {

    private lateinit var launcher: RobloxLauncher
    private lateinit var tracker: ActivityTracker
    private lateinit var optimizations: OptimizationEngine

    override fun onCreate() {
        super.onCreate()
        launcher = RobloxLauncher(this)
        tracker = ActivityTracker()
        optimizations = OptimizationEngine(this)
        lifecycleScope.launch { tracker.load() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(NOTIF_ID, buildNotification(getString(R.string.notif_applying_changes)))
        when (intent?.action) {
            ACTION_LAUNCH -> {
                lifecycleScope.launch {
                    val result = launcher.launch(applyChanges = true)
                    when (result) {
                        is LaunchResult.Success -> {
                            tracker.startSession()
                            updateNotification(getString(R.string.notif_starting_roblox))
                        }
                        else -> Logger.writeLine("LauncherFgSvc", "Launch result: $result")
                    }
                    stopSelf()
                }
            }
            ACTION_CLEANUP -> {
                optimizations.release()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(text: String): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, ChipstrapApp.CHANNEL_LAUNCHER)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(android.app.NotificationManager::class.java) ?: return
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    companion object {
        const val NOTIF_ID = 1001
        const val ACTION_LAUNCH = "com.chipstrap.rbx.action.LAUNCH"
        const val ACTION_CLEANUP = "com.chipstrap.rbx.action.CLEANUP"
    }
}
