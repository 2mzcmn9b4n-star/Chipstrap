package com.chipstrap.rbx.ui.screens.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chipstrap.rbx.R
import kotlinx.coroutines.launch

@Composable
fun ServerInfoScreen(nav: NavController, vm: ServerInfoViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val info by vm.info.collectAsState()
    val loading by vm.loading.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.server_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.server_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Button(onClick = { scope.launch { vm.refresh(context) } }, enabled = !loading) {
            if (loading) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
            Text(stringResource(R.string.server_refresh))
        }

        val s = info
        if (s == null) {
            Text(stringResource(R.string.server_not_connected),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(stringResource(R.string.server_job_id), s.jobId)
                    s.universeId?.let { InfoRow(stringResource(R.string.server_universe_id), it) }
                    s.placeId?.let { InfoRow("Place ID", it) }
                    s.location?.let { InfoRow(stringResource(R.string.server_location), it) }
                    s.pingMs?.let { InfoRow(stringResource(R.string.server_ping), "$it ms") }
                    s.playerCount?.let { InfoRow(stringResource(R.string.server_players), "$it") }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
