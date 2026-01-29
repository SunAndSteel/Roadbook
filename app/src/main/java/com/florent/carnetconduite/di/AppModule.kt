package com.florent.carnetconduite.di

import com.florent.carnetconduite.data.AppDatabase
import com.florent.carnetconduite.domain.usecases.*
import com.florent.carnetconduite.domain.utils.AndroidAppLogger
import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.validators.TripValidator
import com.florent.carnetconduite.repository.SettingsRepository
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.ui.home.HomeViewModel
import com.florent.carnetconduite.ui.history.HistoryViewModel
import com.florent.carnetconduite.ui.settings.SettingsViewModel
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

    // ==================== REPOSITORY LAYER ====================

    single {
        TripRepository(get(), androidContext())
    }

    // ==================== DOMAIN LAYER ====================

    // Utils
    single<AppLogger> { AndroidAppLogger() }
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
        FinishReturnUseCase(
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
        CancelReturnUseCase(
            repository = get(),
            logger = get()
        )
    }

    single {
        EditTripUseCase(
            repository = get(),
            validator = get(),
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
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    /**
     * HistoryViewModel - Gère l'historique des trajets
     */
    viewModel {
        HistoryViewModel(
            get(),
            get(),
            get(),
            get()
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
