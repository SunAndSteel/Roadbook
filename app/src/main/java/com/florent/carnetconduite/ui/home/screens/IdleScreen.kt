package com.florent.carnetconduite.ui.home.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.ui.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun IdleScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state = rememberIdleScreenState()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        IdleScreenContent(state = state)
        IdleScreenPrimaryAction(state = state, viewModel = viewModel)
    }
}

@Stable
class IdleScreenState {
    var startKm by mutableStateOf("")
    var startPlace by mutableStateOf("")
    var conditions by mutableStateOf("")
    var guide by mutableStateOf("1")
    var guideExpanded by mutableStateOf(false)
    var advancedExpanded by mutableStateOf(false)
}

@Composable
fun rememberIdleScreenState(): IdleScreenState = remember { IdleScreenState() }

@Composable
fun IdleScreenContent(state: IdleScreenState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Préparer le départ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Renseigne les informations essentielles pour démarrer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.startKm,
                onValueChange = { state.startKm = it },
                label = { Text("Kilométrage départ") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            OutlinedTextField(
                value = state.startPlace,
                onValueChange = { state.startPlace = it },
                label = { Text("Lieu de départ") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        AssistChip(
            onClick = { state.advancedExpanded = !state.advancedExpanded },
            label = {
                Text(
                    text = if (state.advancedExpanded) "Masquer les options" else "Options complémentaires",
                    style = MaterialTheme.typography.labelMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = if (state.advancedExpanded) {
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
                    },
                    contentDescription = null
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )

        AnimatedVisibility(visible = state.advancedExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = state.guideExpanded,
                    onExpandedChange = { state.guideExpanded = !state.guideExpanded }
                ) {
                    OutlinedTextField(
                        value = "Guide ${state.guide}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Accompagnateur") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(state.guideExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = state.guideExpanded,
                        onDismissRequest = { state.guideExpanded = false }
                    ) {
                        listOf("1", "2").forEach { id ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = null
                                        )
                                        Text("Guide $id")
                                    }
                                },
                                onClick = {
                                    state.guide = id
                                    state.guideExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.conditions,
                    onValueChange = { state.conditions = it },
                    label = { Text("Conditions météo (optionnel)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CloudQueue,
                            contentDescription = null
                        )
                    },
                    placeholder = { Text("Ex: Pluie, nuit...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
}

@Composable
fun IdleScreenPrimaryAction(
    state: IdleScreenState,
    viewModel: HomeViewModel
) {
    FilledTonalButton(
        onClick = {
            viewModel.startOutward(
                startKm = state.startKm.toIntOrNull() ?: 0,
                startPlace = state.startPlace,
                conditions = state.conditions,
                guide = state.guide
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Démarrer le trajet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
