package com.chipstrap.rbx.ui.screens.fflags

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chipstrap.rbx.R
import com.chipstrap.rbx.fflags.presets.FFlagPresets
import com.chipstrap.rbx.fflags.repository.FFlagEntry
import kotlinx.coroutines.launch

@Composable
fun FFlagsScreen(nav: NavController, vm: FFlagsViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { vm.load() }

    val flags by vm.flags.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<FFlagEntry?>(null) }
    var showWarning by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { vm.exportTo(context, it) }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { scope.launch { vm.importFrom(context, it) } }
    }

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.fflags_title), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.fflags_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Presets
            Text(stringResource(R.string.menu_fastflags_section_presets), style = MaterialTheme.typography.titleMedium)
            FFlagPresets.all.forEach { preset ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(preset.titleRes), style = MaterialTheme.typography.titleSmall)
                        Text(
                            stringResource(preset.descRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { scope.launch { vm.applyPreset(preset) } }) {
                                Text(stringResource(R.string.preset_apply))
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Toolbar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(stringResource(R.string.fflags_add))
                }
                IconButton(onClick = { exportLauncher.launch("chipstrap-fflags.json") }) {
                    Icon(Icons.Default.FileUpload, contentDescription = stringResource(R.string.fflags_export))
                }
                IconButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                    Icon(Icons.Default.Download, contentDescription = stringResource(R.string.fflags_import))
                }
            }

            if (flags.isEmpty()) {
                Text(
                    stringResource(R.string.fflags_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(flags, key = { it.key }) { entry ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.key, style = MaterialTheme.typography.bodyMedium)
                                    Text(entry.value, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { editTarget = entry }) { Text(stringResource(R.string.fflags_edit)) }
                                IconButton(onClick = { scope.launch { vm.delete(entry.key) } }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.fflags_delete))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        EditDialog(
            initial = FFlagEntry("", ""),
            title = stringResource(R.string.fflags_add),
            onConfirm = { e ->
                scope.launch { vm.add(e.key, e.value) }
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }
    editTarget?.let { e ->
        EditDialog(
            initial = e,
            title = stringResource(R.string.fflags_edit),
            onConfirm = { ne ->
                scope.launch { vm.delete(e.key); vm.add(ne.key, ne.value) }
                editTarget = null
            },
            onDismiss = { editTarget = null }
        )
    }
}

@Composable
private fun EditDialog(
    initial: FFlagEntry,
    title: String,
    onConfirm: (FFlagEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var key by remember { mutableStateOf(initial.key) }
    var value by remember { mutableStateOf(initial.value) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = key, onValueChange = { key = it },
                    label = { Text(stringResource(R.string.fflags_key_hint)) })
                OutlinedTextField(value = value, onValueChange = { value = it },
                    label = { Text(stringResource(R.string.fflags_value_hint)) })
            }
        },
        confirmButton = { Button(onClick = { onConfirm(FFlagEntry(key.trim(), value.trim())) }) {
            Text(stringResource(R.string.common_save))
        }},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }}
    )
}
