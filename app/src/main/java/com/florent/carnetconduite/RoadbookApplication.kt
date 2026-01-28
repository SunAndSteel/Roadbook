package com.florent.carnetconduite

import android.app.Application
import com.florent.carnetconduite.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class RoadbookApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialisation de Koin
        startKoin {
            // Logger Koin (vous pouvez ajuster le niveau)
            androidLogger(Level.ERROR)  // ERROR en production, DEBUG en dev

            // Context Android
            androidContext(this@RoadbookApplication)

            // Modules de dépendances
            modules(appModule)
        }
    }
}