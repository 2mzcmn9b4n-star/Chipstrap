package com.chipstrap.rbx.ui.screens.integrations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
fun IntegrationsScreen(nav: NavController, vm: IntegrationsViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.home_profile_card_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.home_profile_card_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Strategy picker
        listOf(
            Triple("shizuku", R.string.strategy_shizuku, R.string.strategy_shizuku_desc),
            Triple("root", R.string.strategy_root, R.string.strategy_root_desc),
            Triple("virtual", R.string.strategy_virtual, R.string.strategy_virtual_desc),
            Triple("local", R.string.strategy_local, R.string.strategy_local_desc)
        ).forEach { (id, titleRes, descRes) ->
            Card(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    RadioButton(
                        selected = state.strategy == id,
                        onClick = { scope.launch { vm.setStrategy(id) } }
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(titleRes), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(descRes), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Text(stringResource(R.string.preferred_roblox_app), style = MaterialTheme.typography.titleMedium)
        listOf(
            "global" to R.string.roblox_global,
            "vng" to R.string.roblox_vng,
            "custom" to R.string.roblox_custom
        ).forEach { (id, labelRes) ->
            Card(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    RadioButton(
                        selected = state.preferredApp == id,
                        onClick = { scope.launch { vm.setPreferredApp(id) } }
                    )
                    Text(stringResource(labelRes), modifier = Modifier.weight(1f).padding(top = 4.dp))
                }
            }
        }

        if (state.preferredApp == "custom") {
            OutlinedTextField(
                value = state.customPackage,
                onValueChange = { scope.launch { vm.setCustomPackage(it) } },
                label = { Text("com.roblox.client…") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
