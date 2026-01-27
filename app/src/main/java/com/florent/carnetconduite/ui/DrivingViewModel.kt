package com.florent.carnetconduite.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.florent.carnetconduite.data.*
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.groupTrips
import com.florent.carnetconduite.repository.TripRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class DrivingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TripRepository

    private val _uiEvents = Channel<UiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    val trips: StateFlow<List<Trip>>
    val tripGroups: StateFlow<List<TripGroup>>
    val activeTrip: StateFlow<Trip?>
    val drivingState: StateFlow<DrivingState>
    val arrivedTrip: StateFlow<Trip?>

    init {
        val tripDao = AppDatabase.getDatabase(application).tripDao()
        repository = TripRepository(tripDao, application)

        trips = repository.allTrips.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Grouper les trips pour l'affichage
        tripGroups = trips.map { tripList ->
            groupTrips(tripList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeTrip = repository.activeTrip.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        arrivedTrip = trips.map { tripList ->
            findArrivedTrip(tripList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        drivingState = trips.map { tripList ->
            computeDrivingState(tripList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DrivingState.IDLE
        )
    }

    private fun findArrivedTrip(tripList: List<Trip>): Trip? {
        // Trouver le dernier trajet aller terminé qui n'a pas de retour associé
        val latestOutward = tripList
            .filter { !it.isReturn && it.status == "COMPLETED" }
            .maxByOrNull { it.id }

        if (latestOutward != null) {
            // Vérifier si un retour existe (READY, ACTIVE, COMPLETED, SKIPPED ou CANCELLED)
            val hasReturn = tripList.any {
                it.pairedTripId == latestOutward.id && it.isReturn
            }

            // Si pas de retour, c'est un trajet "arrivé" en attente de décision
            if (!hasReturn) {
                return latestOutward
            }
        }

        return null
    }

    private fun computeDrivingState(tripList: List<Trip>): DrivingState {
        // Priorité 1 : Retour actif
        if (tripList.any { it.endKm == null && it.isReturn }) {
            return DrivingState.RETURN_ACTIVE
        }

        // Priorité 2 : Aller actif
        if (tripList.any { it.endKm == null && !it.isReturn }) {
            return DrivingState.OUTWARD_ACTIVE
        }

        // Priorité 3 : Retour prêt
        if (tripList.any { it.isReturn && it.status == "READY" }) {
            return DrivingState.RETURN_READY
        }

        // Priorité 4 : Arrivé (dernier aller complété sans retour)
        if (findArrivedTrip(tripList) != null) {
            return DrivingState.ARRIVED
        }

        // Priorité 5 : IDLE ou COMPLETED
        return if (tripList.isEmpty() || tripList.all {
                it.status == "COMPLETED" || it.status == "CANCELLED" || it.status == "SKIPPED"
            }) {
            DrivingState.IDLE
        } else {
            DrivingState.COMPLETED
        }
    }

    fun startOutward(startKm: Int, startPlace: String, conditions: String, guide: String) {
        viewModelScope.launch {
            if (startKm < 0) {
                _uiEvents.send(UiEvent.ShowError("Kilomètres invalides"))
                return@launch
            }

            if (startPlace.isBlank()) {
                _uiEvents.send(UiEvent.ShowError("Veuillez saisir un lieu de départ"))
                return@launch
            }

            val now = System.currentTimeMillis()
            val date = LocalDate.now().toString()

            val trip = Trip(
                startKm = startKm,
                startPlace = startPlace,
                startTime = now,
                status = "ACTIVE",
                conditions = conditions,
                guide = guide,
                date = date
            )

            val id = repository.insert(trip)
            repository.saveOngoingSessionId(id)
            _uiEvents.send(UiEvent.ShowToast("Trajet démarré"))
        }
    }

    // Nouvelle méthode : juste finir l'aller, sans décision
    fun finishOutward(tripId: Long, endKm: Int, endPlace: String) {
        viewModelScope.launch {
            val trip = repository.getTripById(tripId) ?: run {
                _uiEvents.send(UiEvent.ShowError("Trajet introuvable"))
                return@launch
            }

            if (endPlace.isBlank()) {
                _uiEvents.send(UiEvent.ShowError("Veuillez saisir un lieu d'arrivée"))
                return@launch
            }

            // Validation : endKm doit être >= startKm
            if (endKm < trip.startKm) {
                _uiEvents.send(UiEvent.ShowConfirmDialog(
                    title = "Kilomètres incohérents",
                    message = "Le kilométrage d'arrivée ($endKm km) est inférieur au départ (${trip.startKm} km). Confirmer ?",
                    onConfirm = {
                        proceedFinishOutward(tripId, endKm, endPlace)
                    }
                ))
                return@launch
            }

            proceedFinishOutward(tripId, endKm, endPlace)
        }
    }

    private fun proceedFinishOutward(tripId: Long, endKm: Int, endPlace: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.finishTrip(tripId, endKm, endPlace, now)
            repository.clearOngoingSessionId()
            _uiEvents.send(UiEvent.ShowToast("Trajet terminé, choisissez la suite"))
        }
    }

    // Décision après l'arrivée
    fun decideTripType(tripId: Long, prepareReturn: Boolean) {
        viewModelScope.launch {
            val trip = repository.getTripById(tripId) ?: return@launch

            if (prepareReturn) {
                repository.finishAndPrepareReturn(
                    tripId = tripId,
                    endKm = trip.endKm ?: 0,
                    endPlace = trip.endPlace ?: "",
                    endTime = trip.endTime ?: System.currentTimeMillis()
                )
                _uiEvents.send(UiEvent.ShowToast("Retour préparé"))
            } else {
                // Créer un Trip SKIPPED au lieu d'un flag global
                repository.createSkippedReturn(tripId)
                _uiEvents.send(UiEvent.ShowToast("Trajet simple enregistré"))
            }
        }
    }

    fun cancelReturn(returnTripId: Long) {
        viewModelScope.launch {
            _uiEvents.send(UiEvent.ShowConfirmDialog(
                title = "Annuler le retour",
                message = "Voulez-vous vraiment annuler le trajet retour ?",
                onConfirm = {
                    viewModelScope.launch {
                        repository.cancelReturn(returnTripId)
                        _uiEvents.send(UiEvent.ShowToast("Retour annulé"))
                    }
                }
            ))
        }
    }

    fun startReturn(returnTripId: Long, actualStartKm: Int?) {
        viewModelScope.launch {
            val trip = repository.getTripById(returnTripId) ?: run {
                _uiEvents.send(UiEvent.ShowError("Trajet retour introuvable"))
                return@launch
            }

            val kmToUse = actualStartKm ?: trip.startKm

            if (kmToUse < 0) {
                _uiEvents.send(UiEvent.ShowError("Kilomètres invalides"))
                return@launch
            }

            val now = System.currentTimeMillis()
            repository.startReturn(returnTripId, actualStartKm, now)
            _uiEvents.send(UiEvent.ShowToast("Retour démarré"))
        }
    }

    fun finishReturn(tripId: Long, endKm: Int) {
        viewModelScope.launch {
            val trip = repository.getTripById(tripId) ?: run {
                _uiEvents.send(UiEvent.ShowError("Trajet introuvable"))
                return@launch
            }

            // Validation
            if (endKm < trip.startKm) {
                _uiEvents.send(UiEvent.ShowConfirmDialog(
                    title = "Kilomètres incohérents",
                    message = "Le kilométrage d'arrivée ($endKm km) est inférieur au départ (${trip.startKm} km). Confirmer ?",
                    onConfirm = {
                        proceedFinishReturn(tripId, endKm, trip.endPlace ?: "")
                    }
                ))
                return@launch
            }

            proceedFinishReturn(tripId, endKm, trip.endPlace ?: "")
        }
    }

    private fun proceedFinishReturn(tripId: Long, endKm: Int, endPlace: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.finishTrip(tripId, endKm, endPlace, now)
            repository.clearOngoingSessionId()
            _uiEvents.send(UiEvent.ShowToast("Retour terminé"))
        }
    }

    fun editStartTime(tripId: Long, newStartTime: Long) {
        viewModelScope.launch {
            repository.updateStartTime(tripId, newStartTime)
            _uiEvents.send(UiEvent.ShowToast("Heure modifiée"))
        }
    }

    fun editEndTime(tripId: Long, newEndTime: Long) {
        viewModelScope.launch {
            repository.updateEndTime(tripId, newEndTime)
            _uiEvents.send(UiEvent.ShowToast("Heure modifiée"))
        }
    }

    fun editStartKm(tripId: Long, newStartKm: Int) {
        viewModelScope.launch {
            if (newStartKm < 0) {
                _uiEvents.send(UiEvent.ShowError("Kilomètres invalides"))
                return@launch
            }
            repository.updateStartKm(tripId, newStartKm)
            _uiEvents.send(UiEvent.ShowToast("Kilomètres modifiés"))
        }
    }

    fun editEndKm(tripId: Long, newEndKm: Int) {
        viewModelScope.launch {
            if (newEndKm < 0) {
                _uiEvents.send(UiEvent.ShowError("Kilomètres invalides"))
                return@launch
            }
            repository.updateEndKm(tripId, newEndKm)
            _uiEvents.send(UiEvent.ShowToast("Kilomètres modifiés"))
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            _uiEvents.send(UiEvent.ShowConfirmDialog(
                title = "Supprimer le trajet",
                message = "Voulez-vous vraiment supprimer ce trajet ?",
                onConfirm = {
                    viewModelScope.launch {
                        repository.delete(trip)
                        _uiEvents.send(UiEvent.ShowToast("Trajet supprimé"))
                    }
                }
            ))
        }
    }

    fun deleteTripGroup(group: TripGroup) {
        viewModelScope.launch {
            val message = if (group.hasReturn) {
                "Voulez-vous vraiment supprimer ce trajet aller-retour ?"
            } else {
                "Voulez-vous vraiment supprimer ce trajet ?"
            }

            _uiEvents.send(UiEvent.ShowConfirmDialog(
                title = "Supprimer le trajet",
                message = message,
                onConfirm = {
                    viewModelScope.launch {
                        repository.delete(group.outward)
                        group.returnTrip?.let { repository.delete(it) }
                        _uiEvents.send(UiEvent.ShowToast("Trajet supprimé"))
                    }
                }
            ))
        }
    }

    fun getTotalKms(): Int {
        return trips.value
            .filter { it.status == "COMPLETED" }
            .sumOf { it.kmsComptabilises }
    }

    fun exportToCSV(): String {
        val headers = "Séance N°;Date;Guide;Heure début;Heure fin;Km départ;Km fin;Type;Départ;Arrivée;Conditions;Nb kms parcourus;Nb kms comptabilisés\n"

        val completedTrips = trips.value.filter { it.status == "COMPLETED" }

        val rows = completedTrips.mapIndexed { index, trip ->
            val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            val startTime = if (trip.startTime > 0) {
                java.time.LocalTime.ofInstant(java.time.Instant.ofEpochMilli(trip.startTime), java.time.ZoneId.systemDefault())
                    .format(timeFormatter)
            } else ""

            val endTime = trip.endTime?.let {
                java.time.LocalTime.ofInstant(java.time.Instant.ofEpochMilli(it), java.time.ZoneId.systemDefault())
                    .format(timeFormatter)
            } ?: ""

            val type = if (trip.isReturn) "R" else "A"

            "${completedTrips.size - index};${trip.date};${trip.guide};$startTime;$endTime;${trip.startKm};${trip.endKm ?: ""};$type;${trip.startPlace};${trip.endPlace ?: ""};${trip.conditions};${trip.nbKmsParcours};${trip.kmsComptabilises}"
        }.joinToString("\n")

        return headers + rows
    }
}