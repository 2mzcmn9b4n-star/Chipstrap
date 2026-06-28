package com.chipstrap.rbx

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chipstrap.rbx.service.LauncherForegroundService
import com.chipstrap.rbx.ui.screens.about.AboutScreen
import com.chipstrap.rbx.ui.screens.fflags.FFlagsScreen
import com.chipstrap.rbx.ui.screens.home.HomeScreen
import com.chipstrap.rbx.ui.screens.integrations.IntegrationsScreen
import com.chipstrap.rbx.ui.screens.optimizations.OptimizationsScreen
import com.chipstrap.rbx.ui.screens.server.ServerInfoScreen
import com.chipstrap.rbx.ui.theme.ChipstrapTheme
import com.chipstrap.rbx.ui.components.AppScaffold

class MainActivity : ComponentActivity() {

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* ignore */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ensurePermissions()
        setContent {
            ChipstrapTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNav()
                }
            }
        }
    }

    private fun ensurePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    AppScaffold(navController = nav) {
        NavHost(navController = nav, startDestination = "home") {
            composable("home") { HomeScreen(nav) }
            composable("fflags") { FFlagsScreen(nav) }
            composable("optimizations") { OptimizationsScreen(nav) }
            composable("integrations") { IntegrationsScreen(nav) }
            composable("server") { ServerInfoScreen(nav) }
            composable("about") { AboutScreen(nav) }
        }
    }
}
