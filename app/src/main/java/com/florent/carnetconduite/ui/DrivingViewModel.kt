package com.florent.carnetconduite.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.florent.carnetconduite.data.AppDatabase
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.data.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class DrivingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TripRepository

    // Exposition directe du Flow converti en StateFlow pour l'UI
    val trips: StateFlow<List<Trip>>

    var showForm by mutableStateOf(false)
        private set

    var currentTrip by mutableStateOf<Trip?>(null)
        private set

    var editingTrip by mutableStateOf<Trip?>(null)
        private set

    init {
        val tripDao = AppDatabase.getDatabase(application).tripDao()
        repository = TripRepository(tripDao)

        // Convertit le Flow de la DB en StateFlow observable par Compose
        trips = repository.allTrips.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun startNewTrip() {
        // Utilisation de java.time (API moderne)
        val now = LocalDate.now()
        val time = LocalTime.now()

        currentTrip = Trip(
            id = UUID.randomUUID().toString(),
            date = now.toString(), // Format ISO-8601 par défaut (YYYY-MM-DD)
            heureDebut = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            status = "started"
        )
        showForm = true
    }

    fun updateCurrentTrip(trip: Trip) {
        // Recalcul automatique des km parcourus si les valeurs changent
        val kmParcourus = if (trip.kmFin >= trip.kmDepart) trip.kmFin - trip.kmDepart else 0
        currentTrip = trip.copy(nbKmsParcours = kmParcourus)
    }

    fun saveTrip(trip: Trip) {
        viewModelScope.launch {
            if (editingTrip != null) {
                repository.update(trip)
                editingTrip = null
            } else {
                repository.insert(trip)
            }
            currentTrip = null
            showForm = false
        }
    }

    fun endTrip(trip: Trip) {
        val time = LocalTime.now()
        val completedTrip = trip.copy(
            heureFin = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            status = "completed"
        )
        saveTrip(completedTrip)
    }

    fun editTrip(trip: Trip) {
        currentTrip = trip
        editingTrip = trip
        showForm = true
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            repository.delete(trip)
        }
    }

    fun cancelForm() {
        currentTrip = null
        editingTrip = null
        showForm = false
    }

    fun getTotalKms(): Int {
        // Calcul simplifié grâce aux entiers
        return trips.value.sumOf { trip ->
            if (trip.typeTrajet == "A") trip.nbKmsParcours else trip.nbKmsParcours / 2
        }
    }

    // Export simplifié
    fun exportToCSV(): String {
        val headers = "Séance N°;Date;Guide;Heure début;Heure fin;Km départ;Km fin;Type trajet;Départ;Arrivée;Conditions;Nb kms parcourus;Nb kms comptabilisés\n"

        val rows = trips.value.mapIndexed { index, trip ->
            val kmsComptabilises = if (trip.typeTrajet == "A") trip.nbKmsParcours else trip.nbKmsParcours / 2

            "${trips.value.size - index};${trip.date};${trip.guide};${trip.heureDebut};${trip.heureFin};${trip.kmDepart};${trip.kmFin};${trip.depart} - ${trip.arrivee} (${trip.typeTrajet});${trip.depart};${trip.arrivee};${trip.conditions};${trip.nbKmsParcours};$kmsComptabilises"
        }.joinToString("\n")

        return headers + rows
    }
}