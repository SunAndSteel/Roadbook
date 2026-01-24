package com.florent.carnetconduite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.DrivingViewModel
import com.florent.carnetconduite.ui.theme.CarnetConduiteTheme
import com.florent.carnetconduite.ui.theme.ThemeMode
import com.florent.carnetconduite.ui.theme.ThemePreferences
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Activation Edge-to-Edge pour SDK 35+
        enableEdgeToEdge()

        setContent {
            val context = this

            val themeMode by ThemePreferences
                .getThemeMode(context)
                .collectAsState(initial = ThemeMode.Dynamic)

            CarnetConduiteTheme(themeMode = themeMode) {
                DrivingLogApp(
                    themeMode = themeMode,
                    onChangeTheme = {
                        val next = when (themeMode) {
                            ThemeMode.Dynamic -> ThemeMode.Light
                            ThemeMode.Light -> ThemeMode.Dark
                            ThemeMode.Dark -> ThemeMode.Dynamic
                        }

                        lifecycleScope.launch {
                            ThemePreferences.saveThemeMode(context, next)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrivingLogApp(
    viewModel: DrivingViewModel = viewModel() ,
    themeMode: ThemeMode,
    onChangeTheme: () -> Unit
) {
    // Collecte de l'état des trajets depuis le Flow
    val trips by viewModel.trips.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Roadbook") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onChangeTheme) {
                        Icon(
                            imageVector = themeIcon(themeMode),
                            contentDescription = "Changer le thème"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!viewModel.showForm) {
                FloatingActionButton(onClick = { viewModel.startNewTrip() }) {
                    Icon(Icons.Default.Add, "Nouveau trajet")
                }
            }
        }
    ) { padding ->
        // Padding géré automatiquement par le Scaffold (compatible Edge-to-Edge)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth() // <--- AJOUTE ÇA ICI
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Permis Libre M36", style = MaterialTheme.typography.titleMedium)
                        Text("Total trajets: ${trips.size}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        "${viewModel.getTotalKms()} km",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (viewModel.showForm && viewModel.currentTrip != null) {
                TripForm(
                    trip = viewModel.currentTrip!!,
                    isEditing = viewModel.editingTrip != null,
                    onTripChange = { viewModel.updateCurrentTrip(it) },
                    onSave = { viewModel.saveTrip(it) },
                    onEnd = { viewModel.endTrip(it) },
                    onCancel = { viewModel.cancelForm() }
                )
            } else {
                TripsList(
                    trips = trips,
                    onEdit = { viewModel.editTrip(it) },
                    onDelete = { viewModel.deleteTrip(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripForm(
    trip: Trip,
    isEditing: Boolean,
    onTripChange: (Trip) -> Unit,
    onSave: (Trip) -> Unit,
    onEnd: (Trip) -> Unit,
    onCancel: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isEditing) "Modifier le trajet" else "Nouveau trajet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = trip.date,
                onValueChange = { onTripChange(trip.copy(date = it)) },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ... (Dropdown Guide identique au code original) ...
        item {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = "Guide ${trip.guide}",
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
                                onTripChange(trip.copy(guide = id))
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = trip.heureDebut,
                    onValueChange = { onTripChange(trip.copy(heureDebut = it)) },
                    label = { Text("Début") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = trip.heureFin,
                    onValueChange = { onTripChange(trip.copy(heureFin = it)) },
                    label = { Text("Fin") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // 2. Ajout du clavier numérique (KeyboardType.Number)
                OutlinedTextField(
                    value = if (trip.kmDepart == 0) "" else trip.kmDepart.toString(),
                    onValueChange = {
                        // Gestion propre des Int
                        val newVal = it.toIntOrNull() ?: 0
                        onTripChange(trip.copy(kmDepart = newVal))
                    },
                    label = { Text("Km départ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (trip.kmFin == 0) "" else trip.kmFin.toString(),
                    onValueChange = {
                        val newVal = it.toIntOrNull() ?: 0
                        onTripChange(trip.copy(kmFin = newVal))
                    },
                    label = { Text("Km fin") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = trip.depart,
                onValueChange = { onTripChange(trip.copy(depart = it)) },
                label = { Text("Lieu de départ") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = trip.arrivee,
                onValueChange = { onTripChange(trip.copy(arrivee = it)) },
                label = { Text("Lieu d'arrivée") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ... (Dropdown Type Trajet identique) ...
        item {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = if (trip.typeTrajet == "A") "Aller (A)" else "Retour (R)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type de trajet") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Aller (A)") },
                        onClick = { onTripChange(trip.copy(typeTrajet = "A")); expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Retour (R)") },
                        onClick = { onTripChange(trip.copy(typeTrajet = "R")); expanded = false }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = trip.nbKmsParcours.toString(),
                onValueChange = { },
                label = { Text("Km parcourus") },
                enabled = false, // Champ calculé automatiquement
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = trip.conditions,
                onValueChange = { onTripChange(trip.copy(conditions = it)) },
                label = { Text("Météo / Conditions") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (trip.status == "started") {
                    Button(
                        onClick = { onEnd(trip) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Terminer Trajet")
                    }
                } else {
                    Button(
                        onClick = { onSave(trip) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Enregistrer")
                    }
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Annuler")
                }
            }
        }
    }
}

// TripsList et TripCard restent similaires mais utilisent les Int directement
@Composable
fun TripsList(trips: List<Trip>, onEdit: (Trip) -> Unit, onDelete: (Trip) -> Unit) {
    if (trips.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucun trajet enregistré", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(trips) { index, trip ->
                TripCard(trip, trips.size - index, { onEdit(trip) }, { onDelete(trip) })
            }
        }
    }
}

@Composable
fun TripCard(trip: Trip, seanceNumber: Int, onEdit: () -> Unit, onDelete: () -> Unit) {
    // Calcul simplifié
    val kmsComptabilises = if (trip.typeTrajet == "A") trip.nbKmsParcours else trip.nbKmsParcours / 2

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Séance $seanceNumber - ${trip.date}", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${trip.depart} → ${trip.arrivee}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Km: ${trip.kmDepart} → ${trip.kmFin} (${trip.nbKmsParcours} km réels)")
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$kmsComptabilises km",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete") }
                    }
                }
            }
        }
    }
}

@Composable
fun themeIcon(themeMode: ThemeMode) = when (themeMode) {
    ThemeMode.Dynamic -> Icons.Default.AutoAwesome
    ThemeMode.Light -> Icons.Default.LightMode
    ThemeMode.Dark -> Icons.Default.DarkMode
}