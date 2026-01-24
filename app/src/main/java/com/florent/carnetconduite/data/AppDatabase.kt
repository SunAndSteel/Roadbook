package com.florent.carnetconduite.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Trip::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Créer nouvelle table avec structure complète
                database.execSQL("""
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

                // Migrer les données existantes
                database.execSQL("""
                    INSERT INTO trips_new (
                        startKm, endKm, startPlace, endPlace, startTime, endTime,
                        isReturn, status, conditions, guide, date
                    )
                    SELECT 
                        CAST(kmDepart AS INTEGER),
                        CASE WHEN kmFin > 0 THEN CAST(kmFin AS INTEGER) ELSE NULL END,
                        depart,
                        arrivee,
                        timestamp,
                        CASE WHEN heureFin != '' THEN timestamp ELSE NULL END,
                        CASE WHEN typeTrajet = 'R' THEN 1 ELSE 0 END,
                        CASE WHEN status = 'completed' THEN 'COMPLETED' ELSE 'ACTIVE' END,
                        conditions,
                        guide,
                        date
                    FROM trips
                """.trimIndent())

                // Supprimer ancienne table
                database.execSQL("DROP TABLE trips")

                // Renommer nouvelle table
                database.execSQL("ALTER TABLE trips_new RENAME TO trips")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "driving_log_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}