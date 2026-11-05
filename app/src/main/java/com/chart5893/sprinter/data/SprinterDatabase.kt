package com.chart5893.sprinter.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        SetEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SprinterDatabase : RoomDatabase() {
    abstract fun sprinterDao(): SprinterDao
}
