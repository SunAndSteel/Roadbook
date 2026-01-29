package com.florent.carnetconduite.di

import com.florent.carnetconduite.data.AppDatabase
import com.florent.carnetconduite.domain.usecases.*
import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.validators.TripValidator
import com.florent.carnetconduite.repository.SettingsRepository
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.ui.home.HomeViewModel
import com.florent.carnetconduite.ui.history.HistoryViewModel
import com.florent.carnetconduite.ui.settings.SettingsViewModel
import com.florent.carnetconduite.ui.theme.ThemePreferences
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Module Koin refactoré avec ViewModels séparés
 */
val appModule = module {

    // ==================== DATA LAYER ====================

    single {
        AppDatabase.getDatabase(androidContext())
    }

    single {
        get<AppDatabase>().tripDao()
    }

    single {
        SettingsRepository(androidContext())
    }

    single {
        AppPreferences(androidContext())
    }

    // ==================== REPOSITORY LAYER ====================

    single {
        TripRepository(
            tripDao = get(),
            logger = get()
        )
    }

    // ==================== DOMAIN LAYER ====================

    // Utils
    single { AppLogger }
    single { TripValidator }

    // Use Cases
    single {
        ComputeDrivingStateUseCase()
    }

    single {
        StartOutwardUseCase(
            repository = get(),
            validator = get(),
            logger = get()
        )
    }

    single {
        FinishOutwardUseCase(
            repository = get(),
            validator = get(),
            logger = get()
        )
    }

    single {
        StartReturnUseCase(
            repository = get(),
            validator = get(),
            logger = get()
        )
    }

    single {
        DecideTripTypeUseCase(
            repository = get(),
            logger = get()
        )
    }

    single {
        DeleteTripGroupUseCase(
            repository = get(),
            logger = get()
        )
    }

    // ==================== VIEWMODEL LAYER ====================

    /**
     * HomeViewModel - Gère l'état actuel de conduite
     */
    viewModel {
        HomeViewModel(
            computeDrivingStateUseCase = get(),
            startOutwardUseCase = get(),
            finishOutwardUseCase = get(),
            startReturnUseCase = get(),
            decideTripTypeUseCase = get(),
            repository = get()
        )
    }

    /**
     * HistoryViewModel - Gère l'historique des trajets
     */
    viewModel {
        HistoryViewModel(
            repository = get(),
            deleteTripGroupUseCase = get()
        )
    }

    /**
     * SettingsViewModel - Gère les préférences
     */
    viewModel {
        SettingsViewModel(
            repository = get()
        )
    }
}