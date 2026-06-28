package com.chipstrap.rbx.ui.screens.optimizations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chipstrap.rbx.R
import kotlinx.coroutines.launch

@Composable
fun OptimizationsScreen(nav: NavController, vm: OptimizationsViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val s by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.opt_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.opt_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OptRow(
            title = stringResource(R.string.opt_cpu_governor),
            desc = stringResource(R.string.opt_cpu_governor_desc),
            checked = s.cpuGovernor,
            onToggle = { scope.launch { vm.setCpuGovernor(it) } }
        )
        OptRow(
            title = stringResource(R.string.opt_kill_bg),
            desc = stringResource(R.string.opt_kill_bg_desc),
            checked = s.killBg,
            onToggle = { scope.launch { vm.setKillBg(it) } }
        )
        OptRow(
            title = stringResource(R.string.opt_clear_cache),
            desc = stringResource(R.string.opt_clear_cache_desc),
            checked = s.clearCache,
            onToggle = { scope.launch { vm.setClearCache(it) } }
        )
        OptRow(
            title = stringResource(R.string.opt_disable_doze),
            desc = stringResource(R.string.opt_disable_doze_desc),
            checked = s.disableDoze,
            onToggle = { scope.launch { vm.setDisableDoze(it) } }
        )
        OptRow(
            title = stringResource(R.string.opt_gpu_tuning),
            desc = stringResource(R.string.opt_gpu_tuning_desc),
            checked = s.gpuTuning,
            onToggle = { scope.launch { vm.setGpuTuning(it) } }
        )
        OptRow(
            title = stringResource(R.string.opt_bluetooth_audio),
            desc = stringResource(R.string.opt_bluetooth_audio_desc),
            checked = s.btAudio,
            onToggle = { scope.launch { vm.setBtAudio(it) } }
        )
        OptRow(
            title = stringResource(R.string.opt_memory_trim),
            desc = stringResource(R.string.opt_memory_trim_desc),
            checked = s.memoryTrim,
            onToggle = { scope.launch { vm.setMemoryTrim(it) } }
        )
        OptRow(
            title = stringResource(R.string.opt_dns),
            desc = stringResource(R.string.opt_dns_desc),
            checked = s.dns,
            onToggle = { scope.launch { vm.setDns(it) } }
        )
    }
}

@Composable
private fun OptRow(title: String, desc: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(desc, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}
