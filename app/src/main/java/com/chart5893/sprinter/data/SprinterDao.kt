package com.chart5893.sprinter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SprinterDao {
    @Query("SELECT * FROM exercises ORDER BY name")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM routines ORDER BY updatedAt DESC")
    fun observeRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutineExercise(routineExercise: RoutineExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkoutExercise(workoutExercise: WorkoutExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetEntry(set: SetEntryEntity)
}
