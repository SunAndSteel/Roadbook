package com.florent.carnetconduite.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.ui.DrivingViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdleScreen(viewModel: DrivingViewModel) {
    var startKm by remember { mutableStateOf("") }
    var startPlace by remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }
    var guide by remember { mutableStateOf("1") }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DriveEta,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text("Démarrer un nouveau trajet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
                value = startKm,
                onValueChange = { startKm = it },
                label = { Text("Kilométrage départ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = startPlace,
                onValueChange = { startPlace = it },
                label = { Text("Lieu de départ") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = "Guide $guide",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Guide") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("1", "2").forEach { id ->
                        DropdownMenuItem(
                            text = { Text("Guide $id") },
                            onClick = {
                                guide = id
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = conditions,
                onValueChange = { conditions = it },
                label = { Text("Météo / Conditions (optionnel)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.startOutward(
                        startKm = startKm.toIntOrNull() ?: 0,
                        startPlace = startPlace,
                        conditions = conditions,
                        guide = guide
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Démarrer le trajet")
            }
        }
    }
}
