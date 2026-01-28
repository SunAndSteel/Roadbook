package com.florent.carnetconduite.di

import com.florent.carnetconduite.data.AppDatabase
import com.florent.carnetconduite.domain.usecases.*
import com.florent.carnetconduite.domain.utils.AndroidAppLogger
import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.validators.TripValidator
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.ui.DrivingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Module Koin pour l'injection de dépendances de l'application.
 * Définit comment créer et injecter toutes les dépendances.
 */
val appModule = module {

    // ====== DATA LAYER ======

    /**
     * Database Room (Singleton)
     */
    single {
        AppDatabase.getDatabase(androidContext())
    }

    /**
     * DAO (depuis la database)
     */
    single {
        get<AppDatabase>().tripDao()
    }

    /**
     * Repository (Singleton)
     */
    single {
        TripRepository(
            tripDao = get(),
            context = androidContext()
        )
    }

    // ====== DOMAIN LAYER ======

    /**
     * Logger (Singleton)
     */
    single<AppLogger> {
        AndroidAppLogger()
    }

    /**
     * Validator (Object singleton - pas besoin de factory)
     */
    single {
        TripValidator
    }

    // ====== USE CASES ======
    // Factory: nouvelle instance à chaque injection

    factory {
        StartOutwardUseCase(
            repository = get(),
            validator = get(),
            logger = get()
        )
    }

    factory {
        FinishOutwardUseCase(
            repository = get(),
            validator = get(),
            logger = get()
        )
    }

    factory {
        DecideTripTypeUseCase(
            repository = get(),
            logger = get()
        )
    }

    factory {
        StartReturnUseCase(
            repository = get(),
            validator = get(),
            logger = get()
        )
    }

    factory {
        FinishReturnUseCase(
            repository = get(),
            validator = get(),
            logger = get()
        )
    }

    factory {
        CancelReturnUseCase(
            repository = get(),
            logger = get()
        )
    }

    factory {
        EditTripUseCase(
            repository = get(),
            validator = get(),
            logger = get()
        )
    }

    factory {
        ComputeDrivingStateUseCase()
    }

    factory {
        DeleteTripGroupUseCase(
            repository = get(),
            logger = get()
        )
    }

    // ====== PRESENTATION LAYER ======

    /**
     * ViewModel avec injection automatique de tous les use cases
     */
    viewModel {
        DrivingViewModel(
            startOutwardUseCase = get(),
            finishOutwardUseCase = get(),
            decideTripTypeUseCase = get(),
            startReturnUseCase = get(),
            finishReturnUseCase = get(),
            cancelReturnUseCase = get(),
            editTripUseCase = get(),
            computeStateUseCase = get(),
            deleteTripGroupUseCase = get(),
            repository = get()
        )
    }
}