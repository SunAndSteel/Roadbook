package com.florent.carnetconduite

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.TripGroup
import com.florent.carnetconduite.ui.DrivingViewModel
import com.florent.carnetconduite.ui.UiEvent
import com.florent.carnetconduite.ui.theme.CarnetConduiteTheme
import com.florent.carnetconduite.ui.theme.ThemeMode
import com.florent.carnetconduite.ui.theme.ThemePreferences
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Destinations de navigation
sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Accueil", Icons.Default.Home)
    object History : Screen("history", "Historique", Icons.Default.List)
}

val navigationItems = listOf(Screen.Home, Screen.History)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = this
            val themeMode by ThemePreferences.getThemeMode(context)
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
    viewModel: DrivingViewModel = viewModel(),
    themeMode: ThemeMode,
    onChangeTheme: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf<UiEvent.ShowConfirmDialog?>(null) }

    // Écouter les événements UI
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is UiEvent.ShowConfirmDialog -> {
                    showConfirmDialog = event
                }
            }
        }
    }

    // Dialog de confirmation
    showConfirmDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text(dialog.title) },
            text = { Text(dialog.message) },
            confirmButton = {
                Button(onClick = {
                    dialog.onConfirm()
                    showConfirmDialog = null
                }) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }

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
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navigationItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop jusqu'au start destination
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Éviter plusieurs copies de la même destination
                                launchSingleTop = true
                                // Restaurer l'état lors de la re-sélection
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen(viewModel)
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: DrivingViewModel) {
    val tripGroups by viewModel.tripGroups.collectAsState()
    val drivingState by viewModel.drivingState.collectAsState()
    val activeTrip by viewModel.activeTrip.collectAsState()
    val arrivedTrip by viewModel.arrivedTrip.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Stats header
        val completedCount = tripGroups.count { it.isComplete }
        StatsHeader(totalKms = viewModel.getTotalKms(), totalTrips = completedCount)

        Spacer(modifier = Modifier.height(16.dp))

        // État actuel + Actions
        when (drivingState) {
            DrivingState.IDLE -> IdleScreen(viewModel)
            DrivingState.OUTWARD_ACTIVE -> activeTrip?.let { OutwardActiveScreen(it, viewModel) }
            DrivingState.ARRIVED -> arrivedTrip?.let { ArrivedScreen(it, viewModel) }
            DrivingState.RETURN_READY -> {
                tripGroups.flatMap { listOfNotNull(it.returnTrip) }
                    .firstOrNull { it.status == "READY" && it.isReturn }?.let {
                        ReturnReadyScreen(it, viewModel)
                    }
            }
            DrivingState.RETURN_ACTIVE -> activeTrip?.let { ReturnActiveScreen(it, viewModel) }
            DrivingState.COMPLETED -> CompletedScreen()
        }
    }
}

@Composable
fun HistoryScreen(viewModel: DrivingViewModel) {
    val tripGroups by viewModel.tripGroups.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TripGroupsList(
            tripGroups = tripGroups.filter { it.isComplete },
            onDelete = { viewModel.deleteTripGroup(it) },
            onEditStartTime = { trip, time -> viewModel.editStartTime(trip.id, time) },
            onEditEndTime = { trip, time -> viewModel.editEndTime(trip.id, time) },
            onEditStartKm = { trip, km -> viewModel.editStartKm(trip.id, km) },
            onEditEndKm = { trip, km -> viewModel.editEndKm(trip.id, km) }
        )
    }
}

@Composable
fun StatsHeader(totalKms: Int, totalTrips: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Permis Libre M36", style = MaterialTheme.typography.titleMedium)
                Text("Total trajets: $totalTrips", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "$totalKms km",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

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

@Composable
fun OutwardActiveScreen(trip: Trip, viewModel: DrivingViewModel) {
    var endKm by remember { mutableStateOf("") }
    var endPlace by remember { mutableStateOf("") }
    var showEditStartTime by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Trajet en cours (Aller)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Text("Départ: ${trip.startPlace}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text("Km départ: ${trip.startKm}", style = MaterialTheme.typography.bodyMedium)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Début: ${formatTime(trip.startTime)}", style = MaterialTheme.typography.bodyMedium)
                IconButton(onClick = { showEditStartTime = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier l'heure", modifier = Modifier.size(18.dp))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Saisir l'arrivée:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = endKm,
                onValueChange = { endKm = it },
                label = { Text("Kilométrage arrivée") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = endPlace,
                onValueChange = { endPlace = it },
                label = { Text("Lieu d'arrivée") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.finishOutward(
                        tripId = trip.id,
                        endKm = endKm.toIntOrNull() ?: 0,
                        endPlace = endPlace
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Flag, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Arriver")
            }
        }
    }

    if (showEditStartTime) {
        TimePickerDialog(
            initialTime = trip.startTime,
            onDismiss = { showEditStartTime = false },
            onConfirm = { newTime ->
                viewModel.editStartTime(trip.id, newTime)
                showEditStartTime = false
            }
        )
    }
}

@Composable
fun ArrivedScreen(trip: Trip, viewModel: DrivingViewModel) {
    var showEditEndTime by remember { mutableStateOf(false) }
    var showEditEndKm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Trajet terminé !", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Text(
                "${trip.startPlace} → ${trip.endPlace ?: ""}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Km arrivée: ${trip.endKm ?: 0}", style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { showEditEndKm = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier", modifier = Modifier.size(18.dp))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Heure arrivée: ${trip.endTime?.let { formatTime(it) } ?: ""}", style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { showEditEndTime = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Text(
                    "${trip.nbKmsParcours} km",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Que veux-tu faire ?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.decideTripType(tripId = trip.id, prepareReturn = false)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Text("Trajet simple")
                    }
                }

                Button(
                    onClick = {
                        viewModel.decideTripType(tripId = trip.id, prepareReturn = true)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SyncAlt, contentDescription = null)
                        Text("Aller-retour")
                    }
                }
            }
        }
    }

    if (showEditEndTime) {
        TimePickerDialog(
            initialTime = trip.endTime ?: System.currentTimeMillis(),
            onDismiss = { showEditEndTime = false },
            onConfirm = { newTime ->
                viewModel.editEndTime(trip.id, newTime)
                showEditEndTime = false
            }
        )
    }

    if (showEditEndKm) {
        EditKmDialog(
            title = "Modifier km arrivée",
            initialKm = trip.endKm ?: 0,
            onDismiss = { showEditEndKm = false },
            onConfirm = { newKm ->
                viewModel.editEndKm(trip.id, newKm)
                showEditEndKm = false
            }
        )
    }
}

@Composable
fun ReturnReadyScreen(trip: Trip, viewModel: DrivingViewModel) {
    var editedStartKm by remember { mutableStateOf(trip.startKm.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.UTurnLeft,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(8.dp))
                Text("Retour prévu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Text(
                "${trip.startPlace} → ${trip.endPlace ?: ""}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedTextField(
                value = editedStartKm,
                onValueChange = { editedStartKm = it },
                label = { Text("Vérifier km départ retour") },
                supportingText = { Text("Vérifie le compteur avant de démarrer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.startReturn(
                            returnTripId = trip.id,
                            actualStartKm = editedStartKm.toIntOrNull()
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Démarrer retour")
                }

                OutlinedButton(
                    onClick = { viewModel.cancelReturn(trip.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Annuler")
                }
            }
        }
    }
}

@Composable
fun ReturnActiveScreen(trip: Trip, viewModel: DrivingViewModel) {
    var endKm by remember { mutableStateOf("") }
    var showEditStartTime by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Retour en cours", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Text("${trip.startPlace} → ${trip.endPlace ?: ""}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text("Km départ: ${trip.startKm}", style = MaterialTheme.typography.bodyMedium)

            Row(verticalAlignment = Alignment.CenterVertically) {
                val timeDisplay = if (trip.startTime > 0L) formatTime(trip.startTime) else "Maintenant"
                Text("Début: $timeDisplay", style = MaterialTheme.typography.bodyMedium)
                if (trip.startTime > 0L) {
                    IconButton(onClick = { showEditStartTime = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier l'heure", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedTextField(
                value = endKm,
                onValueChange = { endKm = it },
                label = { Text("Kilométrage arrivée") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.finishReturn(
                        tripId = trip.id,
                        endKm = endKm.toIntOrNull() ?: 0
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Flag, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Arriver (fin du retour)")
            }
        }
    }

    if (showEditStartTime && trip.startTime > 0L) {
        TimePickerDialog(
            initialTime = trip.startTime,
            onDismiss = { showEditStartTime = false },
            onConfirm = { newTime ->
                viewModel.editStartTime(trip.id, newTime)
                showEditStartTime = false
            }
        )
    }
}

@Composable
fun CompletedScreen() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Tous les trajets sont terminés", style = MaterialTheme.typography.titleMedium)
                Text("Aucun trajet en cours", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TripGroupsList(
    tripGroups: List<TripGroup>,
    onDelete: (TripGroup) -> Unit,
    onEditStartTime: (Trip, Long) -> Unit,
    onEditEndTime: (Trip, Long) -> Unit,
    onEditStartKm: (Trip, Int) -> Unit,
    onEditEndKm: (Trip, Int) -> Unit
) {
    if (tripGroups.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Aucun trajet terminé",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Vos trajets apparaîtront ici",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tripGroups) { group ->
                TripGroupCard(
                    group = group,
                    onDelete = { onDelete(group) },
                    onEditStartTime = onEditStartTime,
                    onEditEndTime = onEditEndTime,
                    onEditStartKm = onEditStartKm,
                    onEditEndKm = onEditEndKm
                )
            }
        }
    }
}

@Composable
fun TripGroupCard(
    group: TripGroup,
    onDelete: () -> Unit,
    onEditStartTime: (Trip, Long) -> Unit,
    onEditEndTime: (Trip, Long) -> Unit,
    onEditStartKm: (Trip, Int) -> Unit,
    onEditEndKm: (Trip, Int) -> Unit
) {
    var showEditMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (group.hasReturn) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Séance ${group.seanceNumber}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (group.hasReturn) {
                            Spacer(Modifier.width(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                Text("Aller-retour", color = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                    }
                    Text("${group.outward.date}", style = MaterialTheme.typography.bodySmall)
                }

                Row {
                    IconButton(onClick = { showEditMenu = true }) {
                        Icon(Icons.Default.Edit, "Modifier")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Supprimer")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Trajet aller
            TripDetails(
                trip = group.outward,
                label = if (group.hasReturn) "Aller" else null
            )

            // Trajet retour si existe
            if (group.hasReturn) {
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                group.returnTrip?.let { returnTrip ->
                    TripDetails(trip = returnTrip, label = "Retour")
                }
            }

            // Total
            Spacer(Modifier.height(8.dp))
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total comptabilisé", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "${group.totalKms} km",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showEditMenu) {
        EditTripGroupDialog(
            group = group,
            onDismiss = { showEditMenu = false },
            onEditStartTime = onEditStartTime,
            onEditEndTime = onEditEndTime,
            onEditStartKm = onEditStartKm,
            onEditEndKm = onEditEndKm
        )
    }
}

@Composable
fun TripDetails(trip: Trip, label: String?) {
    Column {
        label?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
        }

        Text(
            "${trip.startPlace} → ${trip.endPlace}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            "${formatTime(trip.startTime)} - ${trip.endTime?.let { formatTime(it) } ?: ""}",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            "Km: ${trip.startKm} → ${trip.endKm} (${trip.nbKmsParcours} km)",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun EditTripGroupDialog(
    group: TripGroup,
    onDismiss: () -> Unit,
    onEditStartTime: (Trip, Long) -> Unit,
    onEditEndTime: (Trip, Long) -> Unit,
    onEditStartKm: (Trip, Int) -> Unit,
    onEditEndKm: (Trip, Int) -> Unit
) {
    var showTimePicker by remember { mutableStateOf<Pair<Trip, String>?>(null) }
    var showKmPicker by remember { mutableStateOf<Pair<Trip, String>?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le trajet") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("Que veux-tu modifier ?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                // Aller
                item {
                    Text("--- Aller ---", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                item {
                    Button(
                        onClick = { showTimePicker = group.outward to "start" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Heure départ: ${formatTime(group.outward.startTime)}")
                    }
                }

                item {
                    Button(
                        onClick = { showTimePicker = group.outward to "end" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Heure arrivée: ${group.outward.endTime?.let { formatTime(it) } ?: ""}")
                    }
                }

                item {
                    Button(
                        onClick = { showKmPicker = group.outward to "start" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Km départ: ${group.outward.startKm}")
                    }
                }

                item {
                    Button(
                        onClick = { showKmPicker = group.outward to "end" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Km arrivée: ${group.outward.endKm ?: 0}")
                    }
                }

                // Retour
                if (group.hasReturn && group.returnTrip != null) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("--- Retour ---", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }

                    item {
                        Button(
                            onClick = { showTimePicker = group.returnTrip to "start" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Heure départ: ${formatTime(group.returnTrip.startTime)}")
                        }
                    }

                    item {
                        Button(
                            onClick = { showTimePicker = group.returnTrip to "end" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Heure arrivée: ${group.returnTrip.endTime?.let { formatTime(it) } ?: ""}")
                        }
                    }

                    item {
                        Button(
                            onClick = { showKmPicker = group.returnTrip to "start" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Km départ: ${group.returnTrip.startKm}")
                        }
                    }

                    item {
                        Button(
                            onClick = { showKmPicker = group.returnTrip to "end" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Km arrivée: ${group.returnTrip.endKm ?: 0}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )

    showTimePicker?.let { (trip, type) ->
        TimePickerDialog(
            initialTime = if (type == "start") trip.startTime else trip.endTime ?: System.currentTimeMillis(),
            onDismiss = { showTimePicker = null },
            onConfirm = { newTime ->
                if (type == "start") onEditStartTime(trip, newTime) else onEditEndTime(trip, newTime)
                showTimePicker = null
                onDismiss()
            }
        )
    }

    showKmPicker?.let { (trip, type) ->
        EditKmDialog(
            title = if (type == "start") "Modifier km départ" else "Modifier km arrivée",
            initialKm = if (type == "start") trip.startKm else trip.endKm ?: 0,
            onDismiss = { showKmPicker = null },
            onConfirm = { newKm ->
                if (type == "start") onEditStartKm(trip, newKm) else onEditEndKm(trip, newKm)
                showKmPicker = null
                onDismiss()
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    initialTime: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val instant = Instant.ofEpochMilli(initialTime)
    val localTime = LocalTime.ofInstant(instant, ZoneId.systemDefault())

    var hour by remember { mutableStateOf(localTime.hour.toString()) }
    var minute by remember { mutableStateOf(localTime.minute.toString().padStart(2, '0')) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier l'heure") },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = hour,
                    onValueChange = { if (it.length <= 2) hour = it },
                    label = { Text("HH") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Text(":", style = MaterialTheme.typography.headlineMedium)
                OutlinedTextField(
                    value = minute,
                    onValueChange = { if (it.length <= 2) minute = it },
                    label = { Text("MM") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val h = hour.toIntOrNull() ?: 0
                val m = minute.toIntOrNull() ?: 0
                val newTime = instant.atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(h.coerceIn(0, 23), m.coerceIn(0, 59))
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                onConfirm(newTime)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun EditKmDialog(
    title: String,
    initialKm: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var km by remember { mutableStateOf(initialKm.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = km,
                onValueChange = { km = it },
                label = { Text("Kilomètres") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                km.toIntOrNull()?.let { onConfirm(it) }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun themeIcon(themeMode: ThemeMode) = when (themeMode) {
    ThemeMode.Dynamic -> Icons.Default.AutoAwesome
    ThemeMode.Light -> Icons.Default.LightMode
    ThemeMode.Dark -> Icons.Default.DarkMode
}

fun formatTime(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    val instant = Instant.ofEpochMilli(epochMillis)
    val time = LocalTime.ofInstant(instant, ZoneId.systemDefault())
    return time.format(DateTimeFormatter.ofPattern("HH:mm"))
}