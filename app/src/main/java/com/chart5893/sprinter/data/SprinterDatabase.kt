package com.chart5893.sprinter.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        SetEntryEntity::class,
        MeasurementEntryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SprinterDatabase : RoomDatabase() {
    abstract fun sprinterDao(): SprinterDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS measurement_entries (id TEXT NOT NULL PRIMARY KEY, type TEXT NOT NULL, value REAL NOT NULL, date TEXT NOT NULL)")
            }
        }
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE routine_exercises ADD COLUMN targetWeightGrams INTEGER")
            }
        }
    }
}
