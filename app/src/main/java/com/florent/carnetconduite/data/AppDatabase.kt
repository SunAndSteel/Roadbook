package com.florent.carnetconduite.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Base de données Room de l'application.
 * Version 2: Ajout des TypeConverters pour TripStatus enum.
 */
@Database(entities = [Trip::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)  // ← NOUVEAU: TypeConverters pour TripStatus
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Créer nouvelle table avec structure complète
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS trips_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        startKm INTEGER NOT NULL,
                        endKm INTEGER,
                        startPlace TEXT NOT NULL,
                        endPlace TEXT,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER,
                        isReturn INTEGER NOT NULL DEFAULT 0,
                        pairedTripId INTEGER,
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        conditions TEXT NOT NULL DEFAULT '',
                        guide TEXT NOT NULL DEFAULT '1',
                        date TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())

                // Migrer les données existantes si la table trips existe
                db.execSQL("""
                    INSERT OR IGNORE INTO trips_new (
                        id, startKm, endKm, startPlace, endPlace, startTime, endTime,
                        isReturn, pairedTripId, status, conditions, guide, date
                    )
                    SELECT 
                        id,
                        startKm,
                        endKm,
                        startPlace,
                        endPlace,
                        startTime,
                        endTime,
                        isReturn,
                        pairedTripId,
                        status,
                        conditions,
                        guide,
                        date
                    FROM trips WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE type='table' AND name='trips')
                """.trimIndent())

                // Supprimer ancienne table et renommer
                db.execSQL("DROP TABLE IF EXISTS trips")
                db.execSQL("ALTER TABLE trips_new RENAME TO trips")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "roadbook_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration() // Pour développement
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}